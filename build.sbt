name := "clickhouse-akka-stream"
version := "0.4.3"
organization := "io.findify"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/findify/clickhouse-akka-stream"))

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.12.6", "2.11.12")

lazy val akkaVersion = "2.5.14"
lazy val akkaHttpVersion = "10.1.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.dimafeng" %% "testcontainers-scala" % "0.20.0" % "test",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "joda-time" % "joda-time" % "2.9.9",
  "io.circe" %% "circe-core" % "0.9.3",
  "io.circe" %% "circe-generic" % "0.9.3",
  "io.circe" %% "circe-parser" % "0.9.3",
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
  "com.propensive" %% "magnolia" % "0.10.0"
)

publishMavenStyle := true
//bintrayOrganization := Some("findify")

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)



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