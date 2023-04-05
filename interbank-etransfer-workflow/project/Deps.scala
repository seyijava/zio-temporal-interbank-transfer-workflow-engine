import sbt.Keys.scalaVersion
import sbt._

object Deps {
  lazy val grpcVersion = "1.50.1"
  lazy val zioTemporalVersion = "0.1.0-RC6"
  lazy val tapirVersion = "1.2.9"
  lazy val LogbackVersion = "1.2.11"

  val grpc = Seq("io.grpc" % "grpc-netty" % grpcVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0",
    "ch.qos.logback" % "logback-core" % LogbackVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2",
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
  )

  val temporal = Seq("dev.vhonta" %% "zio-temporal-core" % zioTemporalVersion,
    "dev.vhonta" %% "zio-temporal-protobuf" % zioTemporalVersion,
    "dev.vhonta" %% "zio-temporal-testkit" % zioTemporalVersion,
    "dev.vhonta" %% "zio-temporal-protobuf" % zioTemporalVersion % "protobuf",
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  )

  val zioTapir = Seq("com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion
    )

  val zio = Seq(
    "dev.zio" %% "zio" % "2.0.10",
    "dev.zio" %% "zio-test" % "2.0.10" % Test
  )
}
