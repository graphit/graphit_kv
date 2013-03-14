package com.anahoret.graphit.kv

import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.contrib.pattern.ClusterSingletonManager
import akka.routing.FromConfig
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope

case class Get(key: String)
case class Put(key: String, value: String)
case class Result(key: String, value: Option[String])
case class RequestFailed(reason: String)

class StorageService extends Actor {
  val workerRouter =
    context.actorOf(Props[StorageWorker].withRouter(FromConfig), name = "storageWorkerRouter")

  def receive = {
    case Get(key) => workerRouter.tell(ConsistentHashableEnvelope(key, key), self)
    case result: Result  =>  sender ! result
  }
}

class ServiceFacade extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  var currentMaster: Option[akka.actor.Address] = None

  override def preStart(): Unit = cluster.subscribe(self, classOf[LeaderChanged])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case put: Put => // Do nothing so far
    case Get("my-key") => sender ! Result("my-key", Some("my-value"))
    case state: CurrentClusterState => currentMaster = state.leader
    case LeaderChanged(leader)      => currentMaster = leader
  }
}

object Server {
  def main(args: Array[String]): Unit = {
    createActors(configureSystem(args))
  }

  def configureSystem(args: Array[String]): ActorSystem = {
    if (args.nonEmpty) System.setProperty("akka.remote.netty.tcp.port", args(0))

    ActorSystem("GraphitKVCluster", ConfigFactory.parseString(
      """
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
      """).withFallback(ConfigFactory.load(
    )))
  }

  def createActors(system: ActorSystem) {
    createClusterManager(system)
    createServiceFacade(system)
  }

  def createServiceFacade(system: ActorSystem) {
    system.actorOf(Props[ServiceFacade], name = "serviceFacade")
  }

  def createClusterManager(system: ActorSystem) {
    system.actorOf(Props(new ClusterSingletonManager(
      singletonProps = _ => Props[StorageService], singletonName = "storageService",
      terminationMessage = PoisonPill)), name = "singleton")
  }
}
