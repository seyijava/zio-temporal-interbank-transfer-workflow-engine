package com.digitalPlus.fintech.interbank.workflowEngine.service

import com.digitalPlus.fintech.interbank.workflowEngine.api.{
  APIError,
  InternalServerErrors
}
import com.digitalPlus.fintech.interbank.workflowEngine.api.model.{
  FundTransferRequest,
  FundTransferResponse
}
import com.digitalPlus.fintech.interbank.workflowEngine.infra.{
  AppLayer,
  WorkFlowTaskQueue,
  WorkerLayer
}
import com.digitalPlus.fintech.interbank.workflowEngine.workflow.{
  InterBankTransferActivityImpl,
  InterBankTransferWorkFlow
}
import zio.temporal.activity.ZActivityOptions
import zio.temporal.worker.{ZWorkerFactory, ZWorkerFactoryOptions}
import zio.temporal.{TemporalError, ZRetryOptions}
import zio.temporal.workflow.{
  ZWorkflowClient,
  ZWorkflowServiceStubs,
  ZWorkflowStub
}
import zio.{IO, ZIO, ZLayer, durationInt}

import java.util.UUID

trait WorkflowServiceClient {

  def doTransfer(
      transfer: FundTransferRequest
  ): IO[APIError, FundTransferResponse]

  def getTransferStatus(workFlowId: String): IO[APIError, FundTransferResponse]
}

case class WorkflowServiceClientLive(client: ZWorkflowClient)
    extends WorkflowServiceClient {
  override def doTransfer(
      transfer: FundTransferRequest
  ): IO[APIError, FundTransferResponse] = {
    val transferWorkflow = withErrorHandling {
      val workflowId = UUID.randomUUID().toString
      for {
        workflowClient <- client
          .newWorkflowStub[InterBankTransferWorkFlow]
          .withTaskQueue(WorkFlowTaskQueue.queue)
          .withWorkflowId(workflowId)
          .withWorkflowExecutionTimeout(5.minutes)
          .withWorkflowRunTimeout(10.seconds)
          .withRetryOptions(
            ZRetryOptions.default.withMaximumAttempts(1)
          )
          .build
        _ <- ZIO.logInfo("Trigger Interbank Fund Transfer Saga Workflow")
        result <- ZWorkflowStub.execute(
          workflowClient.transferSagaWorkflow(
            transfer.toTransfer
          )
        )
      } yield FundTransferResponse(
        result.senderBankReference,
        result.receivingBankReference,
        workflowId
      )
    }
    val program = ZIO.serviceWithZIO[ZWorkerFactory] { f =>
      f.use(transferWorkflow)
    }
    program.provide(
      WorkerLayer.worker,
      WorkerLayer.clientOptions,
      WorkerLayer.stubOptions,
      InterBankTransferActivityImpl.make,
      ZWorkerFactory.make,
      ZWorkflowClient.make,
      AppLayer.interBankTransferGatewayClientLayer,
      ZWorkflowServiceStubs.make,
      WorkerLayer.worker,
      ZLayer.succeed(ZWorkerFactoryOptions.default),
      ZActivityOptions.default
    )

  }

  override def getTransferStatus(
      workFlowId: String
  ): IO[APIError, FundTransferResponse] = ???

  private def withErrorHandling[R, E, A](
      thunk: ZIO[R, TemporalError[E], A]
  ): ZIO[R, APIError, A] =
    thunk.mapError { temporalError =>
      InternalServerErrors(temporalError.message)
    }
}

object WorkflowServiceClientLive {
  val live: ZLayer[ZWorkflowClient, Nothing, WorkflowServiceClientLive] =
    ZLayer.fromFunction(WorkflowServiceClientLive(_))
}
