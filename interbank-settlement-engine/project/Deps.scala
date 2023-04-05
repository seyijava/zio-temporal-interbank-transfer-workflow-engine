import sbt.Keys.scalaVersion
import sbt._

object Deps {

  val circeV = "0.13.0"
  val kafkaZioV = "2.1.3"
  val zioV = "2.0.10"
  val doobieV = ""

  val circe = Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "io.circe" %% "circe-generic" % circeV
  )

  val kafkaZIO = Seq("dev.zio" %% "zio-kafka" % kafkaZioV)

  val zioStream = Seq("dev.zio" %% "zio-streams" % zioV)

  val zioCore =
    Seq("dev.zio" %% "zio" % zioV, "dev.zio" %% "zio-test" % zioV % Test)

}
