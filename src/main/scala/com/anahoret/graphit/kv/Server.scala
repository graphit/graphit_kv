package com.anahoret.graphit.kv

import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.pattern.ask
import akka.contrib.pattern.ClusterSingletonManager
import akka.util.Timeout
import akka.routing.FromConfig
import akka.actor.RootActorPath

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope


case class Get(key: String)
case class Put(key: String, value: String)
case class Result(key: String, value: Option[String])
case class RequestFailed(reason: String)

class StorageService extends Actor {
  val workerRouter =
    context.actorOf(Props(new StorageWorker).withRouter(FromConfig), name = "storageWorkerRouter")

  def receive = {
    case Get(key) => workerRouter.tell(ConsistentHashableEnvelope(key, key), self)
    case result: Result  =>  sender ! result
  }
}

class ServiceFacade extends Actor with ActorLogging {
  import context.dispatcher
  val cluster = Cluster(context.system)

  var currentMaster: Option[akka.actor.Address] = None

  override def preStart(): Unit = cluster.subscribe(self, classOf[LeaderChanged])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case _: Get if currentMaster.isEmpty =>
      sender ! RequestFailed("Service unavailable, try again later")
    case request: Get =>
      implicit val timeout = Timeout(5, TimeUnit.SECONDS)
      currentMaster foreach { address =>
        val service = context.actorFor(RootActorPath(address) / "user" / "singleton" / "storageService")
        service ? request recover {
          case _ => RequestFailed("Service unavailable, try again later")
        }
      }
    case state: CurrentClusterState => currentMaster = state.leader
    case LeaderChanged(leader)      => currentMaster = leader
  }
}

object Server {
  def main(args: Array[String]): Unit = {
    if (args.nonEmpty) System.setProperty("akka.remote.netty.tcp.port", args(0))

    val system = ActorSystem("ClusterSystem", ConfigFactory.parseString("""
      akka.actor.deployment {
        /singleton/storageService/storageWorkerRouter {
            router = consistent-hashing
            nr-of-instances = 100
            cluster {
              enabled = on
              max-nr-of-instances-per-node = 3
              allow-local-routees = off
            }
          }
      }
    """).withFallback(ConfigFactory.load()))

    system.actorOf(Props(new ClusterSingletonManager(
      singletonProps = _ => Props[StorageService], singletonName = "storageService",
      terminationMessage = PoisonPill)), name = "singleton")

    system.actorOf(Props[ServiceFacade], name = "serviceFacade")
  }
}
