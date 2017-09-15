package io.sudostream.esandosreader

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import com.softwaremill.macwire._
import io.sudostream.esandosreader.api.http.HttpRoutes
import io.sudostream.esandosreader.api.kafka.{KafkaFlow, StreamingComponents}
import io.sudostream.esandosreader.config.{ActorSystemWrapper, ConfigHelper}
import io.sudostream.esandosreader.dao._

object Main extends App {
  lazy val configHelper: ConfigHelper = wire[ConfigHelper]
  lazy val streamingComponents = wire[StreamingComponents]
  lazy val kafkaFlow: KafkaFlow = wire[KafkaFlow]
  lazy val httpRoutes: HttpRoutes = wire[HttpRoutes]
  lazy val mongoDbConnectionWrapper: MongoDbConnectionWrapper = wire[MongoDbConnectionWrapperImpl]
  lazy val esAndOsDao: EsAndOsReaderDao = wire[MongoDbEsAndOsReaderDao]
  lazy val actorSystemWrapper: ActorSystemWrapper = wire[ActorSystemWrapper]
  lazy val mongoFindQueries: MongoFindQueries = wire[MongoFindQueriesImpl]


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
  }

}

