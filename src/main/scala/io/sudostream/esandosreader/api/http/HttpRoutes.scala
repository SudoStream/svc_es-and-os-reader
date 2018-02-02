package io.sudostream.esandosreader.api.http

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import io.sudostream.esandosreader.api.kafka.StreamingComponents
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.esandosreader.dao.EsAndOsReaderDao
import io.sudostream.timetoteach.kafka.serializing.{ScottishBenchmarksDataSerializer, ScottishEsAndOsDataSerializer}
import io.sudostream.timetoteach.messages.events.SystemEvent
import io.sudostream.timetoteach.messages.systemwide.{SystemEventType, TimeToTeachApplication}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}

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

  val routes = path("api" / "esandos") {
    get {
      val initialRequestReceived = Instant.now().toEpochMilli
      log.debug("Called 'api/esandos' and now getting All the E's and O's from the DAO")

      val scottishEsAndOsDataFuture = dao.extractAllScottishEsAndOs

      Source.fromFuture(scottishEsAndOsDataFuture)
        .map {
          elem =>
            log.info(s"Received all ${elem.allExperiencesAndOutcomes.size} E's and O's from the DAO")

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

      onComplete(scottishEsAndOsDataFuture) {
        case Success(esAndOsData) =>
          val scottishEsAndOsDataSerializer = new ScottishEsAndOsDataSerializer
          val esAndOsDataSerialised = scottishEsAndOsDataSerializer.serialize("ignore", esAndOsData)
          complete(HttpEntity(ContentTypes.`application/octet-stream`, esAndOsDataSerialised))
        case Failure(ex) =>
          failWith(ex)
      }
    }
  } ~ path("api" / "benchmarks") {
    get {
      val initialRequestReceived = Instant.now().toEpochMilli
      log.debug("Called 'api/benchmarks' and now getting All the benchmarks from the DAO")

      val scottishBenchmarksDataFuture = dao.extractAllScottishBenchmarks

      Source.fromFuture(scottishBenchmarksDataFuture)
        .map {
          elem =>
            log.info(s"Received all ${elem.allBenchmarks.size} Benchmarks from the DAO")

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

      onComplete(scottishBenchmarksDataFuture) {
        case Success(benchmarksData) =>
          val scottishBenchmarksDataSerializer = new ScottishBenchmarksDataSerializer
          val benchmarksDataSerialised = scottishBenchmarksDataSerializer.serialize("ignore", benchmarksData)
          complete(HttpEntity(ContentTypes.`application/octet-stream`, benchmarksDataSerialised))
        case Failure(ex) =>
          failWith(ex)
      }
    }
  } ~ health


}
