package io.sudostream.esandosreader.api.kafka

import akka.actor.ActorSystem
import akka.event.Logging
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.sudostream.esandosreader.config.{ActorSystemWrapper, ConfigHelper}
import io.sudostream.timetoteach.kafka.serializing.{GetScottishEsAndOsDataRequestDeserializer, ScottishEsAndOsDataSerializer}
import io.sudostream.timetoteach.messages.scottish.GetScottishEsAndOsDataRequest
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import scala.concurrent.ExecutionContextExecutor

class StreamingComponents(configHelper: ConfigHelper, actorSystemWrapper: ActorSystemWrapper) {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log = Logging(system, this)

  lazy val kafkaConsumerBootServers = configHelper.config.getString("akka.kafka.consumer.bootstrapservers")
  lazy val kafkaProducerBootServers = configHelper.config.getString("akka.kafka.producer.bootstrapservers")

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new GetScottishEsAndOsDataRequestDeserializer)
    .withBootstrapServers(kafkaConsumerBootServers)
    .withGroupId(configHelper.config.getString("akka.kafka.consumer.groupid"))
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new ScottishEsAndOsDataSerializer)
    .withBootstrapServers(kafkaProducerBootServers)

  private[kafka] def definedSource:
  Source[CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest], Control] = {
    val source_topic = configHelper.config.getString("esandos.source_topic")
    log.info(s"Source topic is '$source_topic'")
    Consumer.committableSource(consumerSettings, Subscriptions.topics(source_topic))
  }

  private[kafka] def definedSink: String = {
    val sink_topic = configHelper.config.getString("esandos.sink_topic")
    log.info(s"Sink topic is '$sink_topic'")
    sink_topic
  }

}
