package com.anahoret.graphit.kv

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, PoisonPill, ActorSystem}
import akka.contrib.pattern.ClusterSingletonManager

//case class Get(key: String)
//case class Put(key: String, value: String)
//case class Delete(key: String)

object Server {
  def main(args: Array[String]): Unit = {
    if (args.nonEmpty) System.setProperty("akka.remote.netty.tcp.port", args(0))

    val system = ActorSystem("GraphitKv", ConfigFactory.parseString("""
      akka.actor.deployment {
        /singleton/graphitService/workerRouter {
            router = consistent-hashing
            nr-of-instances = 100
            cluster {
              enabled = on
              max-nr-of-instances-per-node = 3
              allow-local-routees = off
            }
          }
      }
      """).withFallback(ConfigFactory.load()))

    system.actorOf(Props(new ClusterSingletonManager(
      singletonProps = _ ⇒ Props[Service], singletonName = "graphitService",
      terminationMessage = PoisonPill)), name = "singleton")

    system.actorOf(Props[Server], name = "graphitFacade")
  }
}
