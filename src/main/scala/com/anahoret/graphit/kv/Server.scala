package com.anahoret.graphit.kv

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

object Server {

  def main(args: Array[String]): Unit = {
    if (args.nonEmpty) System.setProperty("akka.remote.netty.tcp.port", args(0))

    val system = ActorSystem("ClusterSystem")
    val clusterListener = system.actorOf(Props(new Actor with ActorLogging {
      def receive = {
        case state: CurrentClusterState ⇒
          log.info("Current members: {}", state.members)
        case MemberJoined(member) ⇒
          log.info("Member joined: {}", member)
        case MemberUp(member) ⇒
          log.info("Member is Up: {}", member)
        case UnreachableMember(member) ⇒
          log.info("Member detected as unreachable: {}", member)
        case _: ClusterDomainEvent ⇒ // ignore

      }
    }), name = "clusterListener")

    Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
  }
}
