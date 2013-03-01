package com.anahoret.graphit.kv

import akka.remote.testkit.{MultiNodeSpec, MultiNodeConfig}
import com.typesafe.config.ConfigFactory

import akka.testkit.ImplicitSender

object DefaultConfig extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
}

class ServerUpMultiJvmNode1 extends ServerUpSpec
class ServerUpMultiJvmNode2 extends ServerUpSpec

class ServerUpSpec extends MultiNodeSpec(DefaultConfig) with STMultiNodeSpec with ImplicitSender {

    import DefaultConfig._

    def initialParticipants = roles.size

    "Server" should {
      "be run" in {
        Server.main(Array())
      }
    }
}