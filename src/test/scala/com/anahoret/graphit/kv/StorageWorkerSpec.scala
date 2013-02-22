package com.anahoret.graphit.kv

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.testkit.{TestKit, TestActorRef}
import akka.actor.{Props, ActorSystem, Actor}
import java.util.concurrent.TimeUnit
import actors.threadpool.TimeUnit
import scala.concurrent.duration._

class StorageWorkerSpec(_system: ActorSystem) extends TestKit(_system)
  with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("StorageWorkerSpec"))

  override def afterAll {
    system.shutdown()
  }

  "A StorageWorker" must {

    "answer with the same key" in {
      val worker = system.actorOf(Props[StorageWorker])
      within(2000 millis) {

        worker ! Get("blah")
        expectMsg(Result("blah", "blah"))
      }
    }
  }
}

