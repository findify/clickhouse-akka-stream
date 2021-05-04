name := "clickhouse-akka-stream"
version := "0.4.6"
organization := "io.findify"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/findify/clickhouse-akka-stream"))

scalaVersion := "2.13.5"

crossScalaVersions := Seq("2.12.13", "2.13.5")

lazy val akkaVersion     = "2.5.32"
lazy val akkaHttpVersion = "10.1.14"
lazy val circeVersion    = "0.13.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"          %% "akka-http-core"       % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-testkit"    % akkaHttpVersion % "test",
  "com.typesafe.akka"          %% "akka-slf4j"           % akkaVersion,
  "com.typesafe.akka"          %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream-testkit"  % akkaVersion     % "test",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.3",
  "ch.qos.logback"              % "logback-classic"      % "1.2.3"         % "test",
  "org.scalatest"              %% "scalatest"            % "3.0.9"         % "test",
  "com.dimafeng"               %% "testcontainers-scala" % "0.39.3"        % "test",
  "com.chuusai"                %% "shapeless"            % "2.3.4",
  "joda-time"                   % "joda-time"            % "2.10.10",
  "io.circe"                   %% "circe-core"           % circeVersion,
  "io.circe"                   %% "circe-generic"        % circeVersion,
  "io.circe"                   %% "circe-parser"         % circeVersion,
  "org.scalacheck"             %% "scalacheck"           % "1.14.3"        % "test",
  "com.propensive"             %% "magnolia"             % "0.12.0"
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
