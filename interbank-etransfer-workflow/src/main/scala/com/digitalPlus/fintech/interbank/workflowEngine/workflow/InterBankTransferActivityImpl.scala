package com.digitalPlus.fintech.interbank.workflowEngine.workflow

import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.TransferRequest.TRANSFER_TYPE
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  Amount,
  BankCode,
  TransferRequest,
  AccountHolder => AccountOwner,
  TransferEvent => Event
}
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.ZioTransferGatewayService.InterBankTransferGatewayClient
import com.digitalPlus.fintech.interbank.workflowEngine.transfer_workflow.{
  AccountHolder,
  Transfer,
  TransferError,
  TransferEvent
}
import org.slf4j.LoggerFactory
import zio.{ZIO, ZLayer}
import zio.temporal.activity.{ZActivity, ZActivityOptions}

case class InterBankTransferActivityImpl(
    gatewayClient: InterBankTransferGatewayClient,
    options: ZActivityOptions[Any]
) extends InterBankTransferActivity {

  private lazy val logger = LoggerFactory.getLogger(getClass)

  implicit val activityOptions = options

  override def creditTransfer(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): Either[TransferError, String] = {
    logger.info("Credit Transfer Activity")
    ZActivity.run {
      gatewayClient
        .transfer(
          TransferRequest()
            .withAmount(Amount(amount))
            .withBankCode(BankCode().withCode(bankCode))
            .withAccountHolder(
              AccountOwner()
                .withName(accountHolder.name)
                .withEmail(accountHolder.email)
                .withMobileNumber(accountHolder.mobileNumber)
            )
            .withTransferType(TRANSFER_TYPE.CREDIT)
        )
        .mapBoth(
          status => TransferError().withMessage(status.getDescription),
          resp => resp.transferReferenceNumber
        )
    }
  }

  override def debitTransfer(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): Either[TransferError, String] = {
    logger.info("Debit Transfer Activity")
    ZActivity.run {
      gatewayClient
        .transfer(
          TransferRequest()
            .withAmount(Amount(amount))
            .withBankCode(BankCode().withCode(bankCode))
            .withAccountHolder(
              AccountOwner()
                .withName(accountHolder.name)
                .withEmail(accountHolder.email)
                .withMobileNumber(accountHolder.mobileNumber)
            )
            .withTransferType(TRANSFER_TYPE.DEBIT)
        )
        .mapBoth(
          status => TransferError().withMessage(status.getCode.value() + ""),
          resp => resp.transferReferenceNumber
        )
    }
  }

  override def logTransferEvent(
      event: TransferEvent
  ): Either[TransferError, String] = {
    ZActivity.run {
      gatewayClient
        .logTransferEvent(Event())
        .mapBoth(
          status => TransferError().withMessage("FAILED"),
          resp => ""
        )
    }
  }

  override def reverseTransfer(
      bankCode: String,
      accountHolder: AccountHolder,
      amount: Float,
      narration: String
  ): Unit = ZActivity.run {
    logger.info("Reversal Transfer Activity")
    gatewayClient
      .transfer(
        TransferRequest()
          .withAmount(Amount(amount))
          .withBankCode(BankCode().withCode(bankCode))
          .withAccountHolder(
            AccountOwner()
              .withName(accountHolder.name)
              .withEmail(accountHolder.email)
              .withMobileNumber(accountHolder.mobileNumber)
          )
          .withTransferType(TRANSFER_TYPE.REVERSAL)
      )
      .mapBoth(
        status => TransferError().withMessage("FAILED"),
        resp => ()
      )
  }
}

object InterBankTransferActivityImpl {
  val make: ZLayer[ZActivityOptions[
    Any
  ] with InterBankTransferGatewayClient, Nothing, InterBankTransferActivity] =
    ZLayer.fromFunction(InterBankTransferActivityImpl(_, _))
}
