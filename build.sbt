name := "Graphit KV"

version := "0.0.1"

scalaVersion := "2.10.0"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= {
  val akkaVersion = "2.2-SNAPSHOT"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion
  )
}
















