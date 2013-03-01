package com.anahoret.graphit.kv

import akka.remote.testkit.MultiNodeConfig

object DefaultConfig extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
}