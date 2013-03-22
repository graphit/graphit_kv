package com.anahoret.graphit.kv

import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import scala.concurrent.duration._
import akka.actor.{Actor, Props}
import concurrent.{Future, Await}
import akka.pattern.ask
import akka.util.Timeout

class ServerUpMultiJvmNode1 extends ServerUpSpec
class ServerUpMultiJvmNode2 extends ServerUpSpec

class ServerUpSpec extends MultiNodeSpec(DefaultConfig) with STMultiNodeSpec with ImplicitSender {
  import DefaultConfig._

  implicit val timeout = Timeout(5 seconds)

  override def initialParticipants = roles.size

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
        MemberUp(Member(graphit1Address, MemberStatus.Up,Set.empty)),
        MemberUp(Member(graphit2Address, MemberStatus.Up, Set.empty)))

      Cluster(system).unsubscribe(testActor)

      testConductor.enter("all-up")
    }

    "put and get key-value pairs" in {
      runOn(graphit1) {
        val facade = system.actorFor("user/serviceFacade")

        facade ! Put("my-key", "my-value")

        val future = facade ? Get("my-key")
        val result = Await.result(future, timeout.duration).asInstanceOf[Result]
        result should be(Result("my-key", Some("my-value")))
      }

      testConductor.enter("done-test1")
    }
  }
}