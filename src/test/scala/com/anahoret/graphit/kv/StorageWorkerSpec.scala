package com.anahoret.graphit.kv

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import akka.actor.{Props, ActorSystem, Actor}

class StorageWorkerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("StorageWorkerSpec"))

  override def afterAll { system.shutdown() }

  "A StorageWorker" must {
    "answer with the same key" in {
      val worker = system.actorOf(Props[StorageWorker])
      worker ! Get("blah")
      expectMsg(Result("blah", Some("blah")))
    }
  }
}