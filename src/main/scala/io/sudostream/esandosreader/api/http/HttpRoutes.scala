package io.sudostream.esandosreader.api.http

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import io.sudostream.esandosreader.api.kafka.StreamingComponents
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.esandosreader.dao.EsAndOsReaderDao
import io.sudostream.timetoteach.messages.events.SystemEvent
import io.sudostream.timetoteach.messages.systemwide.{SystemEventType, TimeToTeachApplication}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class HttpRoutes(dao: EsAndOsReaderDao,
                 actorSystemWrapper: ActorSystemWrapper,
                 streamingComponents: StreamingComponents
                )
  extends Health {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log = system.log

  implicit val timeout = Timeout(30.seconds)

  val routes: Route = path("api" / "esandos") {
    get {
      val initialRequestReceived = Instant.now().toEpochMilli
      log.debug("Called 'api/esandos' and now getting All the E's and O's from the DAO")

      val scottishEsAndOsData = dao.extractAllScottishEsAndOs
      log.info(s"Received all ${scottishEsAndOsData.allExperiencesAndOutcomes.size} E's and O's from the DAO")

      Source(List(scottishEsAndOsData))
        .map {
          elem =>
            SystemEvent(
              eventType = SystemEventType.SCOTTISH_ES_AND_OS_REQUESTED_EVENT,
              requestFingerprint = UUID.randomUUID().toString,
              requestingSystem = TimeToTeachApplication.HTTP,
              requestingSystemExtraInfo = Option.empty,
              requestingUsername = Option.empty,
              originalUTCTimeOfRequest = initialRequestReceived,
              processedUTCTime = Instant.now().toEpochMilli,
              extraInfo = Option.empty
            )
        }
        .map {
          elem =>
            new ProducerRecord[Array[Byte], SystemEvent](streamingComponents.definedSystemEventsTopic, elem)
        }
        .runWith(Producer.plainSink(streamingComponents.systemEventProducerSettings))

      complete(HttpEntity(ContentTypes.`application/json`, scottishEsAndOsData.toString))
    }
  } ~ health


}
