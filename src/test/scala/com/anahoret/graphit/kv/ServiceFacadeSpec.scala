package com.anahoret.graphit.kv

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.testkit._
import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.cluster.ClusterEvent.CurrentClusterState
import scala.Some

class ServiceFacadeSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ServiceFacadeSpec"))

  override def afterAll { system.shutdown() }
  implicit val timeout = Timeout(5 seconds)

  "Put" must {
    "proxy received messages to the cluster master" in {
      val master = testActor
      val facade = system.actorOf(Props(new ServiceFacade(master.path.elements)))
      facade ! CurrentClusterState(leader = Some(master.path.address))

      facade ! Put("k", "v")

      expectMsg(10000 millis, Put("k", "v"))
    }
  }
}
