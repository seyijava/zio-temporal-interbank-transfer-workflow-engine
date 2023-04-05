import Deps._
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "interbank-settlement-engine",
    libraryDependencies ++= zioCore ++ zioStream ++ kafkaZIO,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
