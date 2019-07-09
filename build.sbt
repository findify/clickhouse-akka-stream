name := "clickhouse-akka-stream"
version := "0.4.5-M4"
organization := "io.findify"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/findify/clickhouse-akka-stream"))

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.12.8", "2.11.12")

lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.1.8"
lazy val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.dimafeng" %% "testcontainers-scala" % "0.28.0" % "test",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "joda-time" % "joda-time" % "2.10.3",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
  "com.softwaremill" %% "magnolia" % "0.11.1-sml"
)

publishMavenStyle := true
bintrayOrganization := Some("findify")

//publishTo := Some(
//  if (isSnapshot.value)
//    Opts.resolver.sonatypeSnapshots
//  else
//    Opts.resolver.sonatypeStaging
//)



//addCompilerPlugin("io.tryp" % "splain" % "0.2.7" cross CrossVersion.patch)

//scalacOptions ++= Seq("-P:splain:implicits:true","-P:splain:color:false")

pomExtra := (
  <scm>
    <url>git@github.com:findify/clickhouse-akka-stream.git</url>
    <connection>scm:git:git@github.com:findify/clickhouse-akka-stream.git</connection>
  </scm>
    <developers>
      <developer>
        <id>romangrebennikov</id>
        <name>Roman Grebennikov</name>
        <url>http://www.dfdx.me</url>
      </developer>
    </developers>)