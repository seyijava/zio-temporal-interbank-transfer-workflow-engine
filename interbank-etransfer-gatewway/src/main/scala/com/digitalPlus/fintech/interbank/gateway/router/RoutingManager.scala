package com.digitalPlus.fintech.interbank.gateway.router

import com.digitalPlus.fintech.interbank.gateway.provider.fbn.FBNProvider
import com.digitalPlus.fintech.interbank.gateway.provider.gtb.GTBProvider
import com.digitalPlus.fintech.interbank.gateway.router.RoutingManagerLive.BankTransferChannel
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  BankCode,
  TransferRequest,
  TransferResponse
}
import zio.{Task, ULayer, ZIO, ZLayer}

trait RoutingManager {
  def routeBankTransfer(
      request: (BankCode, TransferRequest)
  ): Task[TransferResponse]
}

case class RoutingManagerLive() extends RoutingManager {

  val GTBChannel: BankTransferChannel = {
    case bankChanel if bankChanel._1.code == GTBProvider.GTB =>
      GTBProvider.transfer(bankChanel._2)
  }

  val FBNChannel: BankTransferChannel = {
    case bankChanel if bankChanel._1.code == FBNProvider.FBN =>
      FBNProvider.transfer(bankChanel._2)
  }

  val UKNOWNBankChannel: BankTransferChannel = _ =>
    ZIO.fail(new Throwable("Unknown Bank Channel"))

  val chain: BankTransferChannel =
    GTBChannel orElse FBNChannel orElse UKNOWNBankChannel

  override def routeBankTransfer(
      request: (BankCode, TransferRequest)
  ): Task[TransferResponse] = chain(request)
}

object RoutingManagerLive {

  val live: ULayer[RoutingManagerLive] = ZLayer.succeed(RoutingManagerLive())

  type BankTransferChannel =
    PartialFunction[(BankCode, TransferRequest), Task[TransferResponse]]
}
