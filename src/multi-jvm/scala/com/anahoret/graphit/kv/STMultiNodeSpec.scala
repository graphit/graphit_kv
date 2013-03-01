package com.anahoret.graphit.kv

import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.matchers.ShouldMatchers
import akka.remote.testkit.MultiNodeSpecCallbacks

/**
 * Hooks up MultiNodeSpec with ScalaTest
 */
trait STMultiNodeSpec extends MultiNodeSpecCallbacks with WordSpec with ShouldMatchers with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}