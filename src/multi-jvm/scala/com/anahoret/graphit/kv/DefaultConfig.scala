package com.anahoret.graphit.kv

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object DefaultConfig extends MultiNodeConfig {
  val graphit1 = role("node1")
  val graphit2 = role("node2")

  commonConfig(debugConfig(on = false).withFallback(ConfigFactory.parseString( """
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
              #use-role = storage
            }
          }
        }
      }

      extensions = ["akka.cluster.Cluster"]

      cluster {
        auto-join = off
        auto-down = off
        jmx.enabled = off
        gossip-interval = 200 ms
        leader-actions-interval = 200 ms
        unreachable-nodes-reaper-interval = 200 ms
        periodic-tasks-initial-delay = 300 ms
        publish-stats-interval = 0 s
        failure-detector.heartbeat-interval = 400 ms

        roles = [storage]
      }

      remote {
        log-remote-lifecycle-events = off
        transport = "akka.remote.netty.NettyRemoteTransport"
        netty {
          hostname = "127.0.0.1"
        }
      }

      loggers = ["akka.testkit.TestEventListener"]

      test {
        single-expect-default = 5 s
      }
    }
                                                                               """)))
}