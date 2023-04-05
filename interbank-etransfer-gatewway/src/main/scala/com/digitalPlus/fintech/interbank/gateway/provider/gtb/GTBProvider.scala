package com.digitalPlus.fintech.interbank.gateway.provider.gtb

import com.digitalPlus.fintech.interbank.gateway.provider.TransferService
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.TransferRequest.TRANSFER_TYPE
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  TransferRequest,
  TransferResponse
}
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

case class GTBProvider() extends TransferService {

  override def transferFunds(
      transferRequest: TransferRequest
  ): Task[TransferResponse] = {
    transferRequest.transferType match {
      case TRANSFER_TYPE.CREDIT =>
        ZIO.log(s"GTB Credit Transaction ${transferRequest} ") *> ZIO.succeed(
          TransferResponse().withTransferReferenceNumber(
            UUID.randomUUID().toString
          )
        )

      case TRANSFER_TYPE.DEBIT =>
        ZIO.log(s"GTB Debit Transaction ${transferRequest} ") *> ZIO.succeed(
          TransferResponse().withTransferReferenceNumber(
            UUID.randomUUID().toString
          )
        )

      case TRANSFER_TYPE.REVERSAL =>
        ZIO.log(s"GTB Reversal Transaction ${transferRequest} ") *> ZIO.succeed(
          TransferResponse().withTransferReferenceNumber(
            UUID.randomUUID().toString
          )
        )

      case _ => ZIO.fail(new Throwable())
    }
  }
}

object GTBProvider {
  val GTB = "058"
  val live = ZLayer.succeed(GTBProvider())
  val transfer: TransferRequest => Task[TransferResponse] =
    transferRequest => {
      ZIO
        .serviceWithZIO[GTBProvider](_.transferFunds(transferRequest))
        .provide(live)
    }
}
