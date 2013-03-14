package com.anahoret.graphit.kv

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import concurrent.{Future, Await}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable
import util.Success

class StorageWorkerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with ShouldMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("StorageWorkerSpec"))

  implicit val timeout = Timeout(5 seconds)

  override def afterAll { system.shutdown() }

  private def resultFor(future: Future[Any]): Result = {
    Await.result(future, timeout.duration).asInstanceOf[Result]
  }

  "Get" should {
    "return None if there is no such key" in {
      val worker = system.actorOf(Props(new StorageWorker))
      val future = worker ? Get("unknown-key")
      resultFor(future) should equal (Result("unknown-key", None))
    }

    "return a value for a passed key" in {
      val worker = system.actorOf(Props(new StorageWorker(mutable.Map("known-key" -> "known-value"))))
      val future = worker ? Get("known-key")
      resultFor(future) should equal (Result("known-key", Some("known-value")))
    }
  }

  "Put" should {
    "store provided key-value pair" in {
      val worker = system.actorOf(Props(new StorageWorker))

      val futureForNone = worker ? Get("key")
      resultFor(futureForNone) should equal (Result("key", None))

      worker ! Put("key", "value")

      val future = worker ? Get("key")
      resultFor(future) should equal (Result("key", Some("value")))
    }

    "update stored value" in {
      val worker = system.actorOf(Props(new StorageWorker(mutable.Map("key" -> "value"))))
      worker ! Put("key", "new value")
      val future = worker ? Get("key")
      resultFor(future) should equal (Result("key", Some("new value")))
    }
  }

}