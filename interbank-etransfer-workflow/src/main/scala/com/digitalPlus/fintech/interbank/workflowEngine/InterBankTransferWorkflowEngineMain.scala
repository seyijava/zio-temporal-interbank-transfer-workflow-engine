package com.digitalPlus.fintech.interbank.workflowEngine

import com.digitalPlus.fintech.interbank.workflowEngine.api.BankTransferAPILive
import com.digitalPlus.fintech.interbank.workflowEngine.infra.WorkerLayer
import com.digitalPlus.fintech.interbank.workflowEngine.service.WorkflowServiceClientLive

import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import zio.http.{Server, ServerConfig}
import zio.temporal.workflow.{ZWorkflowClient, ZWorkflowServiceStubs}

object InterBankTransferWorkflowEngineMain extends ZIOAppDefault {

  val server = ZIO.scoped {
    for {
      httpApp <- BankTransferAPILive.routes
      _ <- Server
        .serve(httpApp.withDefaultErrorResponse)
        .provide(
          ServerConfig.live(ServerConfig.default.port(7070)),
          Server.live
        )
    } yield ()
  }

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = {
    val program = (for {
      _ <- server
    } yield ())
    program.provide(
      BankTransferAPILive.live,
      WorkflowServiceClientLive.live,
      ZWorkflowClient.make,
      ZWorkflowServiceStubs.make,
      WorkerLayer.stubOptions,
      WorkerLayer.clientOptions
    )
  }

}
