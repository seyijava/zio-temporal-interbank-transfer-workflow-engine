package com.digitalPlus.fintech.interbank.settlement

import zio.Console.printLine
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object SettlementEngine extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    printLine("Welcome to your first ZIO app!")
}
