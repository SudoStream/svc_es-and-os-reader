package io.sudostream.esandosreader

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import com.softwaremill.macwire._
import io.sudostream.esandosreader.api.http.HttpRoutes
import io.sudostream.esandosreader.api.kafka.KafkaFlow
import io.sudostream.esandosreader.config.{ActorSystemWrapper, ConfigHelper}
import io.sudostream.esandosreader.dao.{EsAndOsReaderDao, MongoDbEsAndOsReaderDao}

object Main extends App {
  //with ConfigHelper with KafkaFlow with HttpRoutes {
  lazy val configHelper: ConfigHelper = wire[ConfigHelper]
  lazy val kafkaFlow: KafkaFlow = wire[KafkaFlow]
  lazy val httpRoutes: HttpRoutes = wire[HttpRoutes]
  lazy val esAndOsDao: EsAndOsReaderDao = new MongoDbEsAndOsReaderDao
  lazy val actorSystemWrapper: ActorSystemWrapper = wire[ActorSystemWrapper]

  implicit val theActorSystem: ActorSystem = actorSystemWrapper.system
  val logger = Logging(theActorSystem, getClass)
  implicit val executor = theActorSystem.dispatcher
  implicit val materializer = actorSystemWrapper.materializer

  kafkaFlow.setUpKafkaFlow()
  setupHttp()

  private def setupHttp() {
    val httpInterface = configHelper.config.getString("http.interface")
    val httpPort = configHelper.config.getInt("http.port")
    val bindingFuture = Http().bindAndHandle(httpRoutes.routes, httpInterface, httpPort)
    logger.info(s"Listening on $httpInterface:$httpPort")

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => actorSystemWrapper.system.terminate()) // and shutdown when done
  }

}

