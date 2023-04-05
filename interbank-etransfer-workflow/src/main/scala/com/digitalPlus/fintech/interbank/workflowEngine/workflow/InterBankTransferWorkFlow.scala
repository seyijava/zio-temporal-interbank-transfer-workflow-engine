package com.digitalPlus.fintech.interbank.workflowEngine.workflow

import com.digitalPlus.fintech.interbank.workflowEngine.transfer_workflow.{FundTransferView, Transfer, TransferError, TransferReference, TransferStatus}
import zio.temporal.{queryMethod, workflowInterface, workflowMethod}

@workflowInterface
trait InterBankTransferWorkFlow {

  @workflowMethod
  def transferSagaWorkflow(
      transfer: Transfer
  ): Either[TransferError, TransferReference]

  @queryMethod
  def getTransferStatus: TransferStatus

}
