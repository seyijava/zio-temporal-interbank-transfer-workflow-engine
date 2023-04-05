import Dep._
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

PB.targets in Compile := Seq(
  scalapb.gen(grpc = true) -> (sourceManaged in Compile).value / "scalapb",
  scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value / "scalapb"
)

lazy val root = (project in file("."))
  .settings(
    name := "interbank-eTransfer-gateway",
    libraryDependencies ++= zioGrpc ++ zioConfig ++ zioTest ++ kafkaZIO ++ logback ++ circe,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
