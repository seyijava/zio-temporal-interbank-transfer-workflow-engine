package com.digitalPlus.fintech.interbank.workflowEngine.workflow

import com.digitalPlus.fintech.interbank.workflowEngine.transfer_workflow.{
  AccountHolder,
  Transfer,
  TransferError,
  TransferEvent
}
import zio.temporal.{activityInterface, activityMethod}

@activityInterface
trait InterBankTransferActivity {

  @activityMethod
  def debitTransfer(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): Either[TransferError, String]
  @activityMethod
  def creditTransfer(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): Either[TransferError, String]

  @activityMethod
  def reverseTransfer(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): Unit

  @activityMethod
  def logTransferEvent(event: TransferEvent): Either[TransferError, String]
}
