package com.digitalPlus.fintech.interbank.settlement.handler

import com.digitalPlus.fintech.interbank.settlement.entity.TransferEvent
import com.digitalPlus.fintech.interbank.settlement.repo.TransferRepository
import zio.{Task, ZLayer}

trait TransferEventHandler {
  def handle(event: TransferEvent): Task[Unit]
}

case class TransferEventHandlerLive(
    settlementRepository: TransferRepository
) extends TransferEventHandler {

  override def handle(event: TransferEvent): Task[Unit] = {
    settlementRepository.save(event)
  }
}

object TransferEventHandlerLive {

  val live: ZLayer[TransferRepository, Nothing, TransferEventHandlerLive] =
    ZLayer.fromFunction(TransferEventHandlerLive(_))
}
