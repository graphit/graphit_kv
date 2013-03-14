package com.anahoret.graphit.kv

import akka.actor.{ActorLogging, Actor}
import akka.cluster.ClusterEvent.{CurrentClusterState, LeaderChanged}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._


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
