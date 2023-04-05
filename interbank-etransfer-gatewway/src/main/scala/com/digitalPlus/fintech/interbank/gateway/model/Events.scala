package com.digitalPlus.fintech.interbank.gateway.model

import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.{
  TransferEvent,
  TransferRequest
}
import io.circe._
import io.circe.generic.semiauto._

object Events {

  implicit class TransferEventImp(event: TransferEvent) {
    def toTransEvent: Event = {
      Event(
        amount = event.getAmount.amount,
        bankCode = event.toBankCode,
        narration = "",
        accountHolder = AccountHolder(
          email = "",
          name = ""
        )
      )

    }
  }
  case class AccountHolder(email: String, name: String)

  case class Event(
      bankCode: String,
      amount: Double,
      narration: String,
      accountHolder: AccountHolder
  )

  implicit val accountHolderDecoder: Decoder[AccountHolder] =
    deriveDecoder[AccountHolder]
  implicit val accountHolderEncoder: Encoder[AccountHolder] =
    deriveEncoder[AccountHolder]

  implicit val transferEventDecoder: Decoder[Event] = deriveDecoder[Event]
  implicit val transferEventEncoder: Encoder[Event] = deriveEncoder[Event]

}
