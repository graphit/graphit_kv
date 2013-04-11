package com.anahoret.graphit.kv

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.contrib.pattern.ClusterSingletonManager
import akka.routing.FromConfig
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import scala.Some

case class Get(key: String)
case class Put(key: String, value: String)
case class Result(key: String, value: Option[String])
case class RequestFailed(reason: String)

class StorageService(handOverData: Option[Any]) extends Actor with ActorLogging {
  val workerRouter =
    context.actorOf(Props(new StorageWorker()).withRouter(FromConfig), name = "storageWorkerRouter")

  def receive = {
    case Put(key, value) => log.error("StorageService PUT"); workerRouter.tell(ConsistentHashableEnvelope(Put(key, value), key), self)
    case Get(key) => log.error("StorageService GET"); workerRouter.tell(ConsistentHashableEnvelope(Get(key), key), self)
//    case result: Result  =>  sender ! result
    case x => log.error("storageService message: ", x)
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
              use-role = storage
            }
          }
      }
      """).withFallback(ConfigFactory.load()))
  }

  def createActors(system: ActorSystem) {
    createClusterManager(system)
    createServiceFacade(system)
  }

  def createServiceFacade(system: ActorSystem) {
    system.actorOf(Props(new ServiceFacade(List("user", "singleton", "storageService"))), name = "serviceFacade")
  }

  def createClusterManager(system: ActorSystem) {
    system.actorOf(Props(new ClusterSingletonManager(
      singletonProps = handOverData => Props(new StorageService(handOverData)),
      role = Some("storage"),
      singletonName = "storageService",
      terminationMessage = PoisonPill)), name = "singleton")
  }
}
