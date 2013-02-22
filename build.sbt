name := "Graphit KV"

version := "0.0.1"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= {
  val akkaVersion = "2.2-SNAPSHOT"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
    "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  )
}
















