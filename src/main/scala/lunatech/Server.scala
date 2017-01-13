package lunatech

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.typesafe.config.ConfigFactory
import lunatech.Endpoints._
import lunatech.controllers.SqlController
import lunatech.filters.HeaderValidator
import lunatech.utils.ServiceHelper

abstract class ServerConfig {
  val port: Int
  val domainHost: String
  val timeout: Long
  val maxConcurrentRequests: Int
  val maxWaiters: Int
  val serviceName: String
}

object Server extends TwitterServer {

  log.info("Serving the Finch Sample application")

  implicit lazy val conf = ConfigFactory.load()
  val serverConfig = new ServerConfig {
    override val domainHost: String         = ""
    override val serviceName: String        = "finchTutorial"
    override val maxConcurrentRequests: Int = conf.getInt("server.maxConcurrentRequests")
    override val timeout: Long              = conf.getLong("server.timeout")
    override val port: Int                  = conf.getInt("server.port")
    override val maxWaiters: Int            = conf.getInt("server.maxWaiters")
  }

  //Default port is used for adminHttpServer
  override def defaultHttpPort: Int = serverConfig.port + 1000

  val serviceName = "finchTutorial"

  lazy val readService: Service[Request, Response] =
    HeaderValidator("datasource").andThen(Service.mk {
      readEndpoints()
    })

  lazy val writeService: Service[Request, Response] =
    Service.mk {
      writeEndpoints()
    }

  lazy val composedService: Service[Request, Response] =
    ServiceHelper.compose(readService, writeService)

  def start(cfg: ServerConfig, service: Service[Request, Response]): ListeningServer =
    Http.server
      .withLabel(serviceName)
      .withStatsReceiver(statsReceiver)
      .withAdmissionControl
      .concurrencyLimit(
        maxConcurrentRequests = cfg.maxConcurrentRequests,
        maxWaiters = cfg.maxWaiters
      )
      .serve(s"${cfg.domainHost}:${cfg.port}", service)

  def main(): Unit = {

    SqlController.createDatabase

    val runningServer = start(serverConfig, composedService)

    onExit {
      println("See you lateeeeeeer!!!!")
    }

    println("CTRL + D to exit")
    scala.io.StdIn.readLine()

    val closing = close()

    Await.all(runningServer, closing)

  }

}
