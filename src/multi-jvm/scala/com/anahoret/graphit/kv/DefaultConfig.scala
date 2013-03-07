package com.anahoret.graphit.kv

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object DefaultConfig extends MultiNodeConfig {
  val graphit1 = role("node1")
  val graphit2 = role("node2")

  commonConfig(ConfigFactory.parseString("""
    akka {
      loglevel = ERROR
      stdout-loglevel = ERROR

      actor {
        provider = "akka.cluster.ClusterActorRefProvider"
        debug {
          receive = on
          lifecycle = on
        }
        deployment {
          /singleton/storageService/storageWorkerRouter {
            router = consistent-hashing
            nr-of-instances = 100
            cluster {
              enabled = on
              max-nr-of-instances-per-node = 3
              allow-local-routees = off
            }
          }
        }
      }

      cluster {
        auto-join = off
        metrics.collector-class = akka.cluster.JmxMetricsCollector
      }

      remote.log-remote-lifecycle-events = off
    }"""))
}