package com.digitalPlus.fintech.interbank.gateway.provider.fbn

import com.digitalPlus.fintech.interbank.gateway.provider.TransferService
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.TransferRequest.TRANSFER_TYPE
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  TransferRequest,
  TransferResponse
}
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

case class FBNProvider() extends TransferService {
  override def transferFunds(
      transferRequest: TransferRequest
  ): Task[TransferResponse] = {
    transferRequest.transferType match {
      case TRANSFER_TYPE.CREDIT =>
        ZIO.log(s"FBN Credit Transaction ${transferRequest} ") *> ZIO.succeed(
          TransferResponse().withTransferReferenceNumber(
            UUID.randomUUID().toString
          )
        )

      case TRANSFER_TYPE.DEBIT =>
        ZIO.log(s"FBN Debit Transaction ${transferRequest} ") *> ZIO.succeed(
          TransferResponse().withTransferReferenceNumber(
            UUID.randomUUID().toString
          )
        )

      case TRANSFER_TYPE.REVERSAL =>
        ZIO.log(s"FBN Reversal Transaction ${transferRequest} ") *> ZIO.succeed(
          TransferResponse().withTransferReferenceNumber(
            UUID.randomUUID().toString
          )
        )

      case _ => ZIO.fail(new Throwable())
    }
  }
}

object FBNProvider {
  val FBN = "011"

  val live = ZLayer.succeed(FBNProvider())
  val transfer: TransferRequest => Task[TransferResponse] =
    (transferRequest) => {
      ZIO
        .serviceWithZIO[FBNProvider](_.transferFunds(transferRequest))
        .provide(live)
    }
}
