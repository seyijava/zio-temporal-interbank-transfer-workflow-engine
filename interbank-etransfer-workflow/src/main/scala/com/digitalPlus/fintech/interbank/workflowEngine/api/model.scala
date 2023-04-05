package com.digitalPlus.fintech.interbank.workflowEngine.api

import com.digitalPlus.fintech.interbank.workflowEngine.transfer_workflow.{
  AccountHolder,
  Transfer
}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object model {

  case class AccountHolderInfo(
      name: String,
      email: String,
      mobileNumber: String
  )

  case class FundTransferRequest(
      fromBankCode: String,
      toBankCode: String,
      amount: Double,
      narration: String,
      sender: AccountHolderInfo,
      receiver: AccountHolderInfo
  ) {
    def toTransfer: Transfer = Transfer()
      .withAmount(this.amount.toFloat)
      .withSender(
        AccountHolder()
          .withName(this.sender.name)
          .withMobileNumber(this.sender.mobileNumber)
      )
      .withReceiver(
        AccountHolder()
          .withName(this.receiver.name)
          .withMobileNumber(this.receiver.mobileNumber)
      )
      .withNarration(this.narration)
      .withSenderBankCode(this.fromBankCode)
      .withReceiverBankCode(this.toBankCode)

  }
  case class FundTransferResponse(
      senderTransferReference: String,
      receiverTransferReference: String,
      workflowId: String
  )

  case class FundTransferStatusResponse(status: String)

  implicit val accountHolderInfoEncoder: JsonEncoder[AccountHolderInfo] =
    DeriveJsonEncoder.gen[AccountHolderInfo]
  implicit val accountHolderInfoRequestDecoder: JsonDecoder[AccountHolderInfo] =
    DeriveJsonDecoder.gen[AccountHolderInfo]

  implicit val fundTransferRequestEncoder: JsonEncoder[FundTransferRequest] =
    DeriveJsonEncoder.gen[FundTransferRequest]
  implicit val fundTransferRequestDecoder: JsonDecoder[FundTransferRequest] =
    DeriveJsonDecoder.gen[FundTransferRequest]

  implicit val fundTransferResponseEncoder: JsonEncoder[FundTransferResponse] =
    DeriveJsonEncoder.gen[FundTransferResponse]
  implicit val fundTransferResponseDecoder: JsonDecoder[FundTransferResponse] =
    DeriveJsonDecoder.gen[FundTransferResponse]

  implicit val fundTransferStatusResponseEncoder: JsonEncoder[FundTransferStatusResponse] =
    DeriveJsonEncoder.gen[FundTransferStatusResponse]
  implicit val fundTransferStatusResponseDecoder: JsonDecoder[FundTransferStatusResponse] =
    DeriveJsonDecoder.gen[FundTransferStatusResponse]

}

sealed trait APIError

case class InternalServerErrors(error: String = "Internal Server error")
    extends APIError

case class NotFound(error: String = "Error Not Found") extends APIError

object APIError {
  implicit val internalServerErrorEncoder: JsonEncoder[InternalServerErrors] =
    DeriveJsonEncoder.gen[InternalServerErrors]
  implicit val internalServerErrorDecoder: JsonDecoder[InternalServerErrors] =
    DeriveJsonDecoder.gen[InternalServerErrors]

  implicit val notFoundEncoder: JsonEncoder[NotFound] =
    DeriveJsonEncoder.gen[NotFound]
  implicit val notFoundDecoder =
    DeriveJsonDecoder.gen[NotFound]

}
