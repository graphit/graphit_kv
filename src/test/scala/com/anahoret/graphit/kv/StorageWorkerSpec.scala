package com.anahoret.graphit.kv

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable

class StorageWorkerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("StorageWorkerSpec"))

  override def afterAll { system.shutdown() }

  "Get" must {
    "return None if there is no such key" in {
      val worker = system.actorOf(Props(new StorageWorker))

      implicit val timeout = Timeout(5 seconds)
      val future = worker ? Get("unknown-key")
      val result = Await.result(future, timeout.duration).asInstanceOf[Result]

      result must equal (Result("unknown-key", None))
    }

    "return a value for a passed key" in {
      val worker = system.actorOf(Props(new StorageWorker(mutable.Map("already-stored-key" -> "already-stored-value"))))

      implicit val timeout = Timeout(5 seconds)
      val future = worker ? Get("already-stored-key")
      val result = Await.result(future, timeout.duration).asInstanceOf[Result]

      result must equal (Result("already-stored-key", Some("already-stored-value")))
    }
  }
}