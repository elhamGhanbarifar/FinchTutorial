import sbt._
import Keys._

name := """finchTutorial"""

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.5"

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

crossScalaVersions := Seq("2.9.2", "2.11.5")

lazy val finagleVersion       = "6.34.0"
lazy val twitterServerVersion = "1.20.0"
lazy val circeVersion         = "0.5.3"
lazy val finchVersion         = "0.11.0-M4"

libraryDependencies ++= Seq(
  "com.twitter"        %% "finagle-http"   % finagleVersion,
  "io.circe"           %% "circe-core"     % circeVersion,
  "io.circe"           %% "circe-generic"  % circeVersion,
  "io.circe"           %% "circe-parser"   % circeVersion,
  "com.github.finagle" %% "finch-core"     % finchVersion,
  "com.github.finagle" %% "finch-circe"    % finchVersion,
  "com.twitter"        %% "twitter-server" % twitterServerVersion,
  "com.typesafe"       % "config"          % "1.3.1",
  "com.typesafe.slick" %% "slick"          % "3.0.0",
  "com.h2database"     % "h2"              % "1.3.175",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

lazy val root =
  project.in(file(".")).settings(mainClass in (Compile, run) := Some("lunatech.Server"))

fork in run := true

reformatOnCompileSettings
