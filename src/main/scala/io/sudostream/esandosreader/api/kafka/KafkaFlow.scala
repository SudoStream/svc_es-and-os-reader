package io.sudostream.esandosreader.api.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.esandosreader.dao.EsAndOsReaderDao
import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{Await, ExecutionContextExecutor, Future}

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

    val flow1 = source.map {
      msg =>
        val getRequestMsg = msg.record.value()
        log.info(s"Received request '$getRequestMsg'")
        getRequestMsg
    }.mapAsync(1) { msg =>
      dao.extractAllScottishEsAndOs
    }.map {
      data =>
      ProducerMessage.Message(
        new ProducerRecord[Array[Byte], ScottishEsAndOsData]
        (streamingComponents.definedSink, data), msg.committableOffset)
    }
  }

  flow.runWith(Producer.commitableSink(streamingComponents.producerSettings))
}

}
