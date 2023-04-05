package com.digitalPlus.fintech.interbank.workflowEngine.workflow

import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.Amount
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.TransferRequest.TRANSFER_TYPE
import com.digitalPlus.fintech.interbank.workflowEngine.transfer_workflow.{
  AccountHolder,
  FundTransferView,
  Transfer,
  TransferError,
  TransferEvent,
  TransferReference,
  TransferStatus
}
import org.slf4j.{LoggerFactory, MDC}
import zio.temporal.ZRetryOptions
import zio.temporal.state.ZWorkflowState
import zio.temporal.workflow.ZWorkflow
import zio._
import zio.temporal.saga.ZSaga

case class FundTransferState(
    state: TransferStatus
)

case class InterBankTransferWorkFlowImpl() extends InterBankTransferWorkFlow {

  private lazy val logger = LoggerFactory.getLogger(getClass)
  MDC.put("transaction_id", ZWorkflow.info.workflowId)

  val activity = ZWorkflow
    .newActivityStub[InterBankTransferActivity]
    .withStartToCloseTimeout(10.seconds)
    .withRetryOptions(ZRetryOptions.default.withMaximumAttempts(1))
    .build

  private val fundTransferState =
    ZWorkflowState.make(FundTransferState(state = TransferStatus.STARTED))

  override def transferSagaWorkflow(
      transfer: Transfer
  ): Either[TransferError, TransferReference] = {
    val saga = for {
      senderReference <- debitTransferSaga(
        transfer.senderBankCode,
        transfer.getSender,
        transfer.amount,
        transfer.narration
      )
      _ = logger.info(s"Initiated Debit Transaction Saga")
      receiverReference <- creditTransferSaga(
        transfer.receiverBankCode,
        transfer.getReceiver,
        transfer.amount,
        transfer.narration
      )
      _ = logger.info(s" Credit Transaction Saga Completed")
      _ <- logTransferEventSaga(
        TransferEvent()
          .withAmount(transfer.amount)
          .withToBankCode(transfer.receiverBankCode)
      )
      _ = logger.info(s" Transfer Event Logged Successfully")
    } yield TransferReference()
      .withSenderBankReference(senderReference)
      .withReceivingBankReference(receiverReference)
    saga.run()
  }

  private def debitTransferSaga(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): ZSaga[TransferError, String] = {
    ZSaga.make(
      handleActivityError(
        activity.debitTransfer(bankCode, accountHolder, amount, narration),
        TRANSFER_TYPE.DEBIT
      )
    )(compensate = {
      if (fundTransferState.snapshot.state.isDebited)
        activity.reverseTransfer(bankCode, accountHolder, amount, narration)
    })
  }

  private def creditTransferSaga(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): ZSaga[TransferError, String] = {
    ZSaga.make {
      handleActivityError(
        activity
          .creditTransfer(bankCode, accountHolder, amount, narration),
        TRANSFER_TYPE.CREDIT
      )
    }(compensate = {
      if (fundTransferState.snapshot.state.isDebited)
        activity
          .reverseTransfer(bankCode, accountHolder, amount, narration)
    })

  }

  override def getTransferStatus: TransferStatus =
    fundTransferState.snapshot.state

  def handleActivityError(
      result: Either[TransferError, String],
      transferType: TRANSFER_TYPE
  ): Either[TransferError, String] = {
    result match {
      case Left(error) if transferType == TRANSFER_TYPE.DEBIT =>
        fundTransferState.update(
          _.copy(state = TransferStatus.DEBIT_CONNECTION_ERROR)
        )
        result
      case Right(_) if transferType == TRANSFER_TYPE.DEBIT =>
        fundTransferState.update(_.copy(state = TransferStatus.DEBITED))
        result
      case Right(_) if transferType == TRANSFER_TYPE.CREDIT =>
        fundTransferState.update(
          _.copy(
            state = TransferStatus.CREDITED
          )
        )
        result
      case Left(error) if transferType == TRANSFER_TYPE.CREDIT =>
        fundTransferState.update(
          _.copy(
            state = TransferStatus.CREDIT_CONNECTION_ERROR
          )
        )
        result
      case Right(transferReference)
          if transferType == TRANSFER_TYPE.DEBIT_REVERSAL =>
        fundTransferState.update(
          _.copy(
            state = TransferStatus.DEBIT_REVERSAL
          )
        )
        result
      case Left(_) if transferType == TRANSFER_TYPE.DEBIT_REVERSAL =>
        fundTransferState.update(
          _.copy(
            state = TransferStatus.DEBIT_CONNECTION_ERROR
          )
        )
        result
    }
  }

  private def logTransferEventSaga(
      transferEvent: TransferEvent
  ): ZSaga[TransferError, String] =
    ZSaga.fromEither(activity.logTransferEvent(transferEvent))

}
