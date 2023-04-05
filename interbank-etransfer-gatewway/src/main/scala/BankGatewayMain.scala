import com.digitalPlus.fintech.interbank.gateway.router.RoutingManagerLive
import com.digitalPlus.fintech.interbank.gateway.server.InterBankTransferGatewayServer
import com.digitalPlus.fintech.interbank.gateway.transfer_gateway_service.ZioTransferGatewayService.ZInterBankTransferGateway
import scalapb.zio_grpc.{Server, ServerLayer, ServiceList}
import zio._

object BankGatewayMain extends ZIOAppDefault {

  val serverLayer
      : ZLayer[Any with ZInterBankTransferGateway[Any], Throwable, Server] = {
    ServerLayer.fromServiceList(
      io.grpc.ServerBuilder.forPort(9090),
      ServiceList.addFromEnvironment[ZInterBankTransferGateway[Any]]
    )
  }

  val appServer = ZLayer.makeSome[Scope, Server](
    serverLayer,
    RoutingManagerLive.live,
    InterBankTransferGatewayServer.live,
    ZLayer.succeed("transferEventTopic")
  )
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    ZIO.log("Starting Server") *> appServer.launch
}
