package com.digitalPlus.fintech.interbank.workflowEngine.infra

import com.digitalPlus.fintech.interbank.workflowEngine.workflow.{InterBankTransferActivity, InterBankTransferWorkFlow, InterBankTransferWorkFlowImpl}
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.temporal.common.converter.{ByteArrayPayloadConverter, DefaultDataConverter, JacksonJsonPayloadConverter, NullPayloadConverter, ProtobufJsonPayloadConverter}
import zio.{ULayer, URLayer, ZIO, ZLayer}
import zio.temporal.worker.ZWorkerFactory
import zio.temporal.workflow.{ZWorkflowClientOptions, ZWorkflowServiceStubsOptions}

object WorkerLayer {


  val defaultDataConverter: DefaultDataConverter = new DefaultDataConverter(
    // order matters!
    Seq(
      new NullPayloadConverter(),
      new ByteArrayPayloadConverter(),
      new ProtobufJsonPayloadConverter(),
      new JacksonJsonPayloadConverter(
        JsonMapper
          .builder()
          .addModule(DefaultScalaModule)
          .build()
      )
    ): _*
  )

  val stubOptions: ULayer[ZWorkflowServiceStubsOptions] = ZLayer.succeed {
    ZWorkflowServiceStubsOptions.default
  }

  val clientOptions: ULayer[ZWorkflowClientOptions] = ZLayer.succeed {
    ZWorkflowClientOptions.default.withDataConverter(defaultDataConverter)
  }
  val worker: URLayer[InterBankTransferActivity with ZWorkerFactory, Unit] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[ZWorkerFactory] { workerFactory =>
        for {
          worker <- workerFactory.newWorker(WorkFlowTaskQueue.queue)
          activityImpl <- ZIO.service[InterBankTransferActivity]
          _ = worker.addActivityImplementation(activityImpl)
          _ = worker
            .addWorkflow[InterBankTransferWorkFlow]
            .from(new InterBankTransferWorkFlowImpl)
        } yield ()
      }
    }
}
