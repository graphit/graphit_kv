package com.anahoret.graphit.kv

import akka.actor.{ActorRef, RootActorPath, ActorLogging, Actor}
import akka.cluster.ClusterEvent.{CurrentClusterState, LeaderChanged}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.util.Timeout
import scala.concurrent.duration._

class ServiceFacade(masterPath: Iterable[String]) extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  var currentMaster: Option[akka.actor.Address] = None

  override def preStart(): Unit = cluster.subscribe(self, classOf[LeaderChanged])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case put: Put => forwardToMaster(put)
    case Get(key) => sender ! Result(key, Some("my-value"))
    case state: CurrentClusterState => currentMaster = state.leader
    case LeaderChanged(leader) => currentMaster = leader
    case error => println("ERROR: ", error)
  }

  private def forwardToMaster(put: Put) {
    implicit val timeout = Timeout(5 seconds)

    currentMaster foreach { address =>
      val master = context.actorFor(RootActorPath(address) / masterPath)
      master ! put
    }
  }
}
