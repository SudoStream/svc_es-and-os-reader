package io.sudostream.esandosreader.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.esandosreader.dao.EsAndOsReaderDao

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class HttpRoutes(dao: EsAndOsReaderDao, actorSystemWrapper: ActorSystemWrapper) extends Health {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer

  implicit val timeout = Timeout(30.seconds)

  val routes: Route = path("api" / "esandos") {
    get {
      val scottishEsAndOsData = dao.extractAllScottishEsAndOs
      complete(HttpEntity(ContentTypes.`application/json`, scottishEsAndOsData.toString))
    }
  } ~ health

}
