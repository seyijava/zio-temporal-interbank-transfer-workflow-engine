package com.digitalPlus.fintech.interbank.settlement.repo

import com.digitalPlus.fintech.interbank.settlement.entity.TransferEvent
import zio.{Task, ZLayer}

trait TransferRepository {
  def save(event: TransferEvent): Task[Unit]
}

case class TransferRepositoryLive() extends TransferRepository {
  override def save(event: TransferEvent): Task[Unit] = ???
}

object TransferRepositoryLive {}

object SQL {}
