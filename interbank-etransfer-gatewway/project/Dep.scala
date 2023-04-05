import sbt.Keys.scalaVersion
import sbt._

object Dep {

  val LogbackVersion = "1.2.11"
  val LogbackEncoderVersion = "4.11"
  val grpcVersion = "1.50.1"
  val kafkaZioV = "2.1.3"
  val zioLoggingVersion = "2.0.0"
  val circeV = "0.13.0"
  val zioV = "2.0.10"
  val doobieV = ""

  val circe = Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "io.circe" %% "circe-generic" % circeV
  )

  val zioGrpc =
    Seq(
      "io.grpc" % "grpc-netty" % grpcVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0"
    )

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.11"
  )

  val zioConfig =
    Seq(
      "dev.zio" %% "zio-json" % "0.3.0-RC10",
      "dev.zio" %% "zio-config" % "3.0.1",
      "dev.zio" %% "zio-config-typesafe" % "3.0.1",
      "dev.zio" %% "zio-config-magnolia" % "3.0.1",
      "dev.zio" %% "zio-logging" % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion
    )

  val kafkaZIO = Seq("dev.zio" %% "zio-kafka" % kafkaZioV)

  val zioTest =
    Seq(
      "dev.zio" %% "zio" % "2.0.10",
      "dev.zio" %% "zio-test" % "2.0.10" % Test
    )

}
