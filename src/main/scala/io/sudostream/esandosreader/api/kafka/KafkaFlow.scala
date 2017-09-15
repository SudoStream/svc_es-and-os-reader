package io.sudostream.esandosreader.api.kafka

import java.time.Instant

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.ProducerMessage
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Source, ZipWith}
import akka.stream.{ClosedShape, Materializer}
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.esandosreader.dao.EsAndOsReaderDao
import io.sudostream.timetoteach.messages.events.SystemEvent
import io.sudostream.timetoteach.messages.scottish.{GetScottishEsAndOsDataRequest, ScottishEsAndOsData}
import io.sudostream.timetoteach.messages.systemwide.SystemEventType
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContextExecutor

class KafkaFlow(streamingComponents: StreamingComponents,
                dao: EsAndOsReaderDao,
                actorSystemWrapper: ActorSystemWrapper
               ) {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log: LoggingAdapter = system.log

  def setUpKafkaFlow(): NotUsed = {
    val runnableGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      val sourceMessages: Source[CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest], Control] =
        streamingComponents.definedSource

      val broadcastGetScottishEsAndOsDataRequestMessage =
        builder.add(Broadcast[CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest]](2))

      val sourceEsAndOsFromDatabase = Source.fromFuture(dao.extractAllScottishEsAndOs)
      val zip = builder.add(ZipWith((msg: CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest],
                                     esAndOs: ScottishEsAndOsData) => (msg, esAndOs)))

      val sinkScottishEsAndOsTopic = Producer.commitableSink(streamingComponents.producerSettings)
      val sinkSystemEventTopic = Producer.plainSink(streamingComponents.systemEventProducerSettings)

      // Graph - Start ========================================================================================
      // MainLine
      sourceMessages ~> broadcastGetScottishEsAndOsDataRequestMessage ~> flowLogEsAndOsRequest ~> zip.in0
      sourceEsAndOsFromDatabase ~> zip.in1
      zip.out ~> createProducerRecordForKafka ~> sinkScottishEsAndOsTopic

      // Side Line
      broadcastGetScottishEsAndOsDataRequestMessage ~> createSystemEventFromIncomingMessage ~> sinkSystemEventTopic
      // Graph - End ==========================================================================================

      ClosedShape
    })

    runnableGraph.run()
  }

  private[kafka] def createSystemEventFromIncomingMessage = {
    Flow[CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest]]
      .map {
        msg =>
          val esAndOsRequest = msg.record.value()

          val systemEvent = SystemEvent(
            eventType = SystemEventType.SCOTTISH_ES_AND_OS_REQUESTED_EVENT,
            requestFingerprint = esAndOsRequest.requestFingerprint,
            requestingSystem = esAndOsRequest.requestingSystem,
            requestingSystemExtraInfo = esAndOsRequest.requestingSystemExtraInfo,
            requestingUsername = esAndOsRequest.requestingUsername,
            originalUTCTimeOfRequest = esAndOsRequest.originalUTCTimeOfRequest,
            processedUTCTime = Instant.now().toEpochMilli,
            extraInfo = Option.empty
          )

          new ProducerRecord[Array[Byte], SystemEvent](streamingComponents.definedSystemEventsTopic, systemEvent)
      }
  }

  private[kafka] def flowLogEsAndOsRequest = {
    Flow[CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest]]
      .map {
        msg =>
          val esAndOsRequest = msg.record.value()
          log.info("Received request " + esAndOsRequest.toString)
          msg
      }
  }

  private[kafka] def createProducerRecordForKafka = {
    Flow[(CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest], ScottishEsAndOsData)]
      .map {
        tupleRequestMsgWithEsAndOs =>
          val getRequestMsg = tupleRequestMsgWithEsAndOs._1
          val data = tupleRequestMsgWithEsAndOs._2

          val msgToCommit = ProducerMessage.Message(
            new ProducerRecord[Array[Byte], ScottishEsAndOsData]
            (streamingComponents.definedSink, data), getRequestMsg.committableOffset)

          msgToCommit
      }
  }

}
