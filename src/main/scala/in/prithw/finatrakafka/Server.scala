package in.prithw.finatrakafka

import in.prithw.finatrakafka.modules.ServiceSwaggerModule
import in.prithw.finatrakafka.controllers.AdminController
import in.prithw.finatrakafka.controllers.MainController
import in.prithw.finatrakafka.filters.CommonFilters
import in.prithw.finatrakafka.util.AppConfigLib._
import in.prithw.finatrakafka.util.PipeOperator._
import com.jakehschwartz.finatra.swagger.DocsController
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.Var
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import perfolation._

object ServerMain extends Server

class Server extends HttpServer {
  val health = Var("good")

  implicit lazy val scheduler: SchedulerService = Scheduler.io("in.prithw.finatrakafka")

  override protected def modules = Seq(ServiceSwaggerModule)

  override def defaultHttpPort = getConfig[String]("FINATRA_HTTP_PORT").fold(":9999")(x => p":$x")
  override val name            = "in.prithw.finatrakafka-FinatraKafka"

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[DocsController]
      .add[AdminController]
      .add[MainController]
      .|>(_ => ())
  }
}
