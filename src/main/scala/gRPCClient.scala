import HelperUtils.ObtainConfigReference

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}
import com.karanmalhotra.protos.logfile.{LogCheckerGrpc, LogRequest}
import com.karanmalhotra.protos.logfile.LogCheckerGrpc.LogCheckerBlockingStub
import com.typesafe.config.{Config, ConfigFactory}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}

object gRPCClient {
  val config: Config  = ConfigFactory.load()
  /* fetching TIME AND DELTA_TIME from Configs */
  val TIME = config.getString("time")
  val DELTATIME = config.getString("delta_time")
  def apply(host: String, port: Int): gRPCClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
    val blockingStub = LogCheckerGrpc.blockingStub(channel)
    new gRPCClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    /* create a client on localhost with the port entered in the config */
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
    /* take name as parameter */
    logger.info("value of name is "+name)
    /* split name on , to get Time and Delta Time */
    val params = name.split(",")
    logger.info("T="+params(0)+" and dT="+params(1))
    /* send a request to the server */
    val request = LogRequest(name)
    /* Based on 200/404 error, display responses accordingly */
    try {
      val response = blockingStub.checkTimestampInLogs(request)
      /* display a list of hashed messages */
      logger.info("Result: " + response.message)
      response.message
    }
    catch {
          /* logs dont exist in the T-dT to T+dT timeframe */
      case e: StatusRuntimeException =>
        logger.info("Result: False. Logs dont exist in that timeframe")
        "Output is false"
    }
  }
}
