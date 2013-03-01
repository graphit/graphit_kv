package com.anahoret.graphit.kv

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender

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