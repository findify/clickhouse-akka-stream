name := "clickhouse-akka-stream"

version := "0.5.0"

organization := "io.findify"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/findify/clickhouse-akka-stream"))

scalaVersion := "2.13.7"

crossScalaVersions := Seq("2.12.13", "2.13.7")

lazy val akkaVersion     = "2.6.18"
lazy val akkaHttpVersion = "10.2.7"
lazy val circeVersion    = "0.14.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"          %% "akka-http-core"       % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-testkit"    % akkaHttpVersion % "test",
  "com.typesafe.akka"          %% "akka-slf4j"           % akkaVersion,
  "com.typesafe.akka"          %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream-testkit"  % akkaVersion     % "test",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.4",
  "ch.qos.logback"              % "logback-classic"      % "1.2.10"        % "test",
  "org.scalatest"              %% "scalatest"            % "3.0.9"         % "test",
  "com.dimafeng"               %% "testcontainers-scala" % "0.39.12"       % "test",
  "com.chuusai"                %% "shapeless"            % "2.3.7",
  "joda-time"                   % "joda-time"            % "2.10.13",
  "io.circe"                   %% "circe-core"           % circeVersion,
  "io.circe"                   %% "circe-generic"        % circeVersion,
  "io.circe"                   %% "circe-parser"         % circeVersion,
  "org.scalacheck"             %% "scalacheck"           % "1.15.4"        % "test",
  "com.propensive"             %% "magnolia"             % "0.17.0"
)

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/findify/clickhouse-akka-stream"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/findify/clickhouse-akka-stream"),
    "scm:git@github.com:findify/clickhouse-akka-stream.git"
  )
)
developers := List(
  Developer(
    id = "romangrebennikov",
    name = "Roman Grebennikov",
    email = "roman@findify.io",
    url = url("https://findify.io/")
  )
)
