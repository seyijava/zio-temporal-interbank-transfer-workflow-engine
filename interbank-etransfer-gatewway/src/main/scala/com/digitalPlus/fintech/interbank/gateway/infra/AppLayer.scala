package com.digitalPlus.fintech.interbank.gateway.infra

import zio.ZLayer
import zio.kafka.producer.{Producer, ProducerSettings}

object AppLayer {
  val producerLayer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(
      Producer.make(
        ProducerSettings(List("localhost:29092"))
      )
    )
}
