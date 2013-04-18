package com.anahoret.graphit.kv

import scala.collection.mutable
import akka.actor.{ActorLogging, Actor}

class StorageWorker(storage: mutable.Map[String, String] = mutable.Map.empty[String, String]) extends Actor {
  def receive = {
    case Get(key) => sender ! lookup(key)
    case Put(key, value) => store(key, value)
  }

  private def lookup(key: String): Result = Result(key, storage.get(key))

  private def store(key: String, value: String): Unit = storage(key) = value
}
