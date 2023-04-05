package com.digitalPlus.fintech.interbank.gateway.provider

import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  TransferRequest,
  TransferResponse
}
import zio.Task

trait TransferService {
  def transferFunds(transferRequest: TransferRequest): Task[TransferResponse]
}
