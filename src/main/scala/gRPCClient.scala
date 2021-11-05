import HelperUtils.ObtainConfigReference

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}
import com.karanmalhotra.protos.logfile.{LogCheckerGrpc, LogRequest}
import com.karanmalhotra.protos.logfile.LogCheckerGrpc.LogCheckerBlockingStub
import com.typesafe.config.{Config, ConfigFactory}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}

object gRPCClient {
  val config: Config  = ConfigFactory.load()
  val TIME = config.getString("time")
  val DELTATIME = config.getString("delta_time")
  def apply(host: String, port: Int): gRPCClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
    val blockingStub = LogCheckerGrpc.blockingStub(channel)
    new gRPCClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = gRPCClient("localhost", ConfigFactory.load().getInt("port"))
    try {
      val user = args.headOption.getOrElse(TIME+","+DELTATIME)
      println("Your calculated result is: "+client.greet(user))
    } finally {
      client.shutdown()
    }
  }
}

class gRPCClient private(private val channel: ManagedChannel,private val blockingStub: LogCheckerBlockingStub) {
  private[this] val logger = Logger.getLogger(classOf[gRPCClient].getName)

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(7, TimeUnit.SECONDS)
  }

  def greet(name: String): String = {
    logger.info("value of name is "+name)
    val params = name.split(",")
    logger.info("T="+params(0)+" and dT="+params(1))
    val request = LogRequest(name)
    try {
      val response = blockingStub.checkTimestampInLogs(request)
      logger.info("Result: " + response.message)
      response.message
    }
    catch {
      case e: StatusRuntimeException =>
        logger.info("Result: False. Logs dont exist in that timeframe")
        "Output is false"
    }
  }
}
