package com.digitalPlus.fintech.interbank.workflowEngine.infra

import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.ZioTransferGatewayService.InterBankTransferGatewayClient
import com.digitalPlus.fintech.interbank.workflowEngine.api.{
  APIError,
  InternalServerErrors
}
import io.grpc.ManagedChannelBuilder
import zio.Layer
import scalapb.zio_grpc.ZManagedChannel

object AppLayer {
  val interBankTransferGatewayClientLayer
      : Layer[APIError, InterBankTransferGatewayClient] =
    InterBankTransferGatewayClient
      .live(
        ZManagedChannel(
          ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext()
        )
      )
      .mapError(ex => InternalServerErrors("FAILE CONNECTION"))
}
