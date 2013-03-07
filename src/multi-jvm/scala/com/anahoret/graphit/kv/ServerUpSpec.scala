package com.anahoret.graphit.kv

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import akka.remote
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{MemberStatus, Member, Cluster}
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp

class ServerUpMultiJvmNode1 extends ServerUpSpec
class ServerUpMultiJvmNode2 extends ServerUpSpec

class ServerUpSpec extends MultiNodeSpec(DefaultConfig) with STMultiNodeSpec with ImplicitSender {

    import DefaultConfig._

    def initialParticipants = roles.size

    "Server" should {
      "be up" in {
        Cluster(system).subscribe(testActor, classOf[MemberUp])
        expectMsgClass(classOf[CurrentClusterState])

        val graphit1Address = node(graphit1).address
        val graphit2Address = node(graphit2).address

        Cluster(system) join graphit2Address

        runOn(graphit1) {
          Server.createActors(system)
          testConductor.enter("gpaphit1-up")
        }

        runOn(graphit2) {
          testConductor.enter("gpaphit1-up")
          Server.createActors(system)
        }

        expectMsgAllOf(
          MemberUp(Member(graphit1Address, MemberStatus.Up)),
          MemberUp(Member(graphit2Address, MemberStatus.Up)))

        Cluster(system).unsubscribe(testActor)

        testConductor.enter("all-up")
      }
    }
}