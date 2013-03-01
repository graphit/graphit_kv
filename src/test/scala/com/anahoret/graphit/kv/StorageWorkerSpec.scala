package com.anahoret.graphit.kv

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import concurrent.{Future, Await}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable

class StorageWorkerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("StorageWorkerSpec"))

  implicit val timeout = Timeout(5 seconds)

  override def afterAll { system.shutdown() }

  private def resultFor(future: Future[Any]): Result = {
    Await.result(future, timeout.duration).asInstanceOf[Result]
  }

  "Get" must {
    "return None if there is no such key" in {
      val worker = system.actorOf(Props(new StorageWorker))
      val future = worker ? Get("unknown-key")
      resultFor(future) must equal (Result("unknown-key", None))
    }

    "return a value for a passed key" in {
      val worker = system.actorOf(Props(new StorageWorker(mutable.Map("already-stored-key" -> "already-stored-value"))))
      val future = worker ? Get("already-stored-key")
      resultFor(future) must equal (Result("already-stored-key", Some("already-stored-value")))
    }
  }

  "Put" must {
    "store provided key-value pair" in {
      val worker = system.actorOf(Props(new StorageWorker))

      val futureForNone = worker ? Get("key")
      resultFor(futureForNone) must equal (Result("key", None))

      worker ! Put("key", "value")

      val future = worker ? Get("key")
      resultFor(future) must equal (Result("key", Some("value")))
    }

    "update stored value" in {
      val worker = system.actorOf(Props(new StorageWorker(mutable.Map("key" -> "value"))))
      worker ! Put("key", "new value")
      val future = worker ? Get("key")
      resultFor(future) must equal (Result("key", Some("new value")))
    }
  }

}