package com.digitalPlus.fintech.interbank.workflowEngine.api

import com.digitalPlus.fintech.interbank.workflowEngine.api.model.{
  FundTransferRequest,
  FundTransferResponse
}
import com.digitalPlus.fintech.interbank.workflowEngine.service.WorkflowServiceClient
import sttp.model.StatusCode
import sttp.tapir.{EndpointOutput, endpoint, oneOf, oneOfVariant, statusCode}
import sttp.tapir.json.zio.jsonBody
import zio.{Task, ZIO, ZLayer}
import zio.http.{Http, HttpApp, Request, Response}
import sttp.tapir.generic.auto._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._

trait BankTransferAPI {
  def httpRoutes: ZIO[Any, Nothing, HttpApp[Any, Throwable]]
}

case class BankTransferAPILive(workflowServiceClient: WorkflowServiceClient)
    extends BankTransferAPI {

  private val baseEndpoint = endpoint.in("api")

  val defaultErrorOutputs: EndpointOutput.OneOf[APIError, APIError] =
    oneOf[APIError](
      oneOfVariant(
        statusCode(StatusCode.InternalServerError)
          .and(jsonBody[InternalServerErrors])
      ),
      oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound]))
    )

  val fundTransferEndpoint = baseEndpoint.post
    .in("transfer")
    .in("fund")
    .in(jsonBody[FundTransferRequest])
    .out(jsonBody[FundTransferResponse])
    .errorOut(defaultErrorOutputs)

  val fundTransferRoute = fundTransferEndpoint.zServerLogic(request =>
    workflowServiceClient.doTransfer(request).provide()
  )

  private val swaggerEndPoints: List[ServerEndpoint[Any, Task]] =
    SwaggerInterpreter()
      .fromEndpoints[Task](
        List(fundTransferEndpoint),
        "InterBank Fund Transfer API Gateway",
        "1.0"
      )

  private val allRoutes: Http[Any, Throwable, Request, Response] =
    ZioHttpInterpreter().toHttp(
      List(
        fundTransferRoute
      )
    )

  override def httpRoutes: ZIO[Any, Nothing, HttpApp[Any, Throwable]] = for {
    routeHttp <- ZIO.succeed(allRoutes)
    endpointHttp <- ZIO.succeed(ZioHttpInterpreter().toHttp(swaggerEndPoints))
  } yield routeHttp ++ endpointHttp

}

object BankTransferAPILive {
  val live: ZLayer[WorkflowServiceClient, Nothing, BankTransferAPI] =
    ZLayer.fromFunction(BankTransferAPILive(_))

  val routes: ZIO[BankTransferAPI, Nothing, HttpApp[Any, Throwable]] =
    ZIO.serviceWithZIO[BankTransferAPI](_.httpRoutes)
}
