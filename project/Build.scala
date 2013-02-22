import sbt._
import Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm }

object GraphitKVBuild extends Build {

  lazy val buildSettings = Defaults.defaultSettings ++ multiJvmSettings ++ Seq(
    organization := "com.anahoret.graphit",
    version := "0.0.1",
    scalaVersion := "2.10.0",
    crossPaths := false
  )

  lazy val graphitKV = Project(
    id = "graphit-kv",
    base = file("."),
    settings = buildSettings ++
      Seq(libraryDependencies ++= Dependencies.graphitKV)
  ) configs(MultiJvm)

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((_, testResults), (_, multiJvmResults)) =>
          val results = testResults ++ multiJvmResults
          (Tests.overall(results.values), results)
      }
  )

  object Dependencies {
    val akkaVersion = "2.2-SNAPSHOT"
    val graphitKV = Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest"     %% "scalatest" % "1.9.1" % "test"
    )
  }
}

