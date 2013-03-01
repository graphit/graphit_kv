package com.anahoret.graphit.kv

import scala.collection.mutable
import akka.actor.Actor

class StorageWorker(storage: mutable.Map[String, String] = mutable.Map.empty[String, String]) extends Actor {

  def receive = {
    case Get(key) ⇒ sender ! lookup(key)
  }

  private def lookup(key: String): Result = {
    try {
      Result(key, Some(storage(key)))
    } catch {
      case e: java.util.NoSuchElementException => Result(key, None)
    }
  }
}
