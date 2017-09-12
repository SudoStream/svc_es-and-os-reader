package io.sudostream.esandosreader.api.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerMessage
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.esandosreader.dao.EsAndOsReaderDao
import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{ExecutionContextExecutor, Future}

class KafkaFlow(streamingComponents: StreamingComponents,
                dao: EsAndOsReaderDao,
                actorSystemWrapper: ActorSystemWrapper
               ) {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log = system.log

  def setUpKafkaFlow(): Future[Done] = {
    val source = streamingComponents.definedSource
    source.map {
      msg =>
        val getRequestMsg = msg.record.value()
        log.info(s"Received request '$getRequestMsg'")

        val scottishEsAndOsData = dao.extractAllScottishEsAndOs

        ProducerMessage.Message(
          new ProducerRecord[Array[Byte], ScottishEsAndOsData]
          (streamingComponents.definedSink, scottishEsAndOsData), msg.committableOffset)
    }.runWith(Producer.commitableSink(streamingComponents.producerSettings))
  }

}
