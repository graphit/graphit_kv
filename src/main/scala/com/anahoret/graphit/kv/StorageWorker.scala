package com.anahoret.graphit.kv

import akka.actor.Actor

class StorageWorker extends Actor {
  val storage = scala.collection.mutable.Map("already-stored-key" -> "already-stored-value")

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
