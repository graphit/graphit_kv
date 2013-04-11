package com.anahoret.graphit.kv

import akka.cluster.{ClusterEvent, Member, MemberStatus, Cluster}
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
    "be up" in within(30 seconds) {
      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])

      Cluster(system) join node(graphit1).address

      expectMsgAllOf(
        MemberUp(Member(node(graphit1).address, MemberStatus.Up, Set("storage"))),
        MemberUp(Member(node(graphit2).address, MemberStatus.Up, Set("storage"))))

      Server.createActors(system)

      expectMsgType[ClusterEvent.RoleLeaderChanged]
      Cluster(system).unsubscribe(testActor)

      testConductor.enter("all-up")
    }

    "put and get key-value pairs" in within(15 seconds) {

      runOn(graphit1) {
        val facade = system.actorFor("user/serviceFacade")
        facade ! Put("my-key", "my-value")

        awaitCond {
          val future = facade ? Get("my-key")
          val result = Await.result(future, timeout.duration).asInstanceOf[Result]
          result match {
            case Result("my-key", Some("my-value")) => true
            case _ => false
          }
        }
      }

      runOn(graphit2) {
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