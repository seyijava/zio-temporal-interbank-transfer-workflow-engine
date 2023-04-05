package com.digitalPlus.fintech.interbank.gateway.server

import com.digitalPlus.fintech.interbank.gateway.infra.AppLayer
import com.digitalPlus.fintech.interbank.gateway.router.RoutingManager
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  TransferEvent,
  TransferRequest,
  TransferResponse,
  VoidResponse
}
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.ZioTransferGatewayService.InterBankTransferGateway
import io.grpc.Status
import zio.kafka.producer.Producer
import zio.{ZIO, ZLayer}
import java.util.UUID
import io.circe.syntax._
import com.digitalPlus.fintech.interbank.gateway.model.Events.TransferEventImp
import zio.kafka.serde.Serde
case class InterBankTransferGatewayServer(
    routingManager: RoutingManager,
    topic: String
) extends InterBankTransferGateway {

  override def transfer(
      transfer: TransferRequest
  ): ZIO[Any, Status, TransferResponse] = {
    routingManager
      .routeBankTransfer(transfer.getBankCode, transfer)
      .mapError(ex => Status.INTERNAL.withDescription(ex.getMessage))
  }

  override def logTransferEvent(
      transferEvent: TransferEvent
  ): ZIO[Any, Status, VoidResponse] = {

    Producer
      .produce(
        topic,
        UUID.randomUUID().toString,
        transferEvent.toTransEvent.asJson.toString(),
        Serde.string,
        Serde.string
      )
      .provide(AppLayer.producerLayer)
      .mapBoth(
        ex => Status.INTERNAL.withDescription(ex.getMessage),
        _ => VoidResponse()
      )

  }
}

object InterBankTransferGatewayServer {

  val live: ZLayer[
    RoutingManager with String,
    Nothing,
    InterBankTransferGatewayServer
  ] =
    ZLayer.fromFunction(InterBankTransferGatewayServer(_, _))
}
