import com.karanmalhotra.protos.logfile.{LogCheckerGrpc, LogReply, LogRequest}
import com.typesafe.config.{Config, ConfigFactory}
import gRPCServer.config
import io.grpc.{Server, ServerBuilder}

import java.util.logging.{Level, Logger}
import scala.concurrent.{ExecutionContext, Future}
import spray.json._

object gRPCServer {
  val config: Config  = ConfigFactory.load()
  private val logger = Logger.getLogger(classOf[gRPCServer].getName)

  def main(args: Array[String]): Unit = {
    val server: gRPCServer = new gRPCServer(ExecutionContext.global)
    startServer(server)
    blockServerUntilShutdown(server)
  }

  def startServer(server: gRPCServer): Unit = {
    server.start()
  }

  def blockServerUntilShutdown(server: gRPCServer): Unit =  {
    server.blockUntilShutdown()
  }

  def stopServer(server: gRPCServer): Unit =  {
    server.stop()
  }

  private val port:Int = ConfigFactory.load().getInt("port")
}

class gRPCServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = _
  private[this] val logger = Logger.getLogger(classOf[gRPCServer].getName)
  private def start(): Unit = {
    server = ServerBuilder.forPort(gRPCServer.port).addService(LogCheckerGrpc.bindService(new LogCheckerImpl, executionContext)).build.start
    gRPCServer.logger.info("Server started, listening on " + gRPCServer.port)
    sys.addShutdownHook {
      logger.warning("*** shutting down gRPC server since JVM is shutting down")
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      logger.warning("*** server shut down")
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class LogCheckerImpl extends LogCheckerGrpc.LogChecker {
    override def checkTimestampInLogs(req: LogRequest): Future[LogReply] = {
      val AWS_URL = config.getString("aws_url")
      //Read input parameters
      val params = req.tandDT.split(",")
      val T = params(0)
      val dT = params(1)

      //Call Lambda API Gateway
      val responseAWS = scala.io.Source.fromURL(AWS_URL+"?T="+T+"&dT="+dT)
      val result = responseAWS.mkString
      val json = result.parseJson.asJsObject
      logger.info("json is "+json)
      val reply = if (json.fields("isPresent").toString() == "\"True\"") {
        LogReply(message = json.fields("content").toString())
      }
      else {
        LogReply(message = json.fields("isPresent").toString())
      }
      responseAWS.close()
      Future.successful(reply)
      responseAWS.close()
      Future.successful(reply)
    }
  }
}
