package io.sudostream.esandosreader.api.kafka

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.sudostream.esandosreader.config.{ActorSystemWrapper, ConfigHelper}
import io.sudostream.timetoteach.kafka.serializing.{GetScottishEsAndOsDataRequestDeserializer, ScottishEsAndOsDataSerializer, SystemEventSerializer}
import io.sudostream.timetoteach.messages.events.SystemEvent
import io.sudostream.timetoteach.messages.scottish.{GetScottishEsAndOsDataRequest, ScottishEsAndOsData}
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import scala.concurrent.ExecutionContextExecutor

class StreamingComponents(configHelper: ConfigHelper, actorSystemWrapper: ActorSystemWrapper) {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log: LoggingAdapter = system.log

  lazy val kafkaConsumerBootServers: String = configHelper.config.getString("akka.kafka.consumer.bootstrapservers")
  lazy val kafkaProducerBootServers: String = configHelper.config.getString("akka.kafka.producer.bootstrapservers")
  lazy val kafkaSaslJaasUsername: String = configHelper.config.getString("akka.kafka.saslJassUsername")
  lazy val kafkaSaslJaasPassword: String = configHelper.config.getString("akka.kafka.saslJassPassword")
  lazy val kafkaSaslJaasConfig: String = s"org.apache.kafka.common.security.scram.ScramLoginModule required " +
    s"""username="$kafkaSaslJaasUsername" password="$kafkaSaslJaasPassword";"""

  val consumerSettings: ConsumerSettings[Array[Byte], GetScottishEsAndOsDataRequest] =
    ConsumerSettings(system, new ByteArrayDeserializer, new GetScottishEsAndOsDataRequestDeserializer)
      .withBootstrapServers(kafkaConsumerBootServers)
      .withGroupId(configHelper.config.getString("akka.kafka.consumer.groupid"))
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
      .withProperty(SaslConfigs.SASL_JAAS_CONFIG, kafkaSaslJaasConfig)
      .withProperty(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256")
      .withProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")

  val producerSettings: ProducerSettings[Array[Byte], ScottishEsAndOsData] =
    ProducerSettings(system, new ByteArraySerializer, new ScottishEsAndOsDataSerializer)
      .withBootstrapServers(kafkaProducerBootServers)
      .withProperty(SaslConfigs.SASL_JAAS_CONFIG, kafkaSaslJaasConfig)
      .withProperty(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256")
      .withProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")

  val systemEventProducerSettings: ProducerSettings[Array[Byte], SystemEvent] =
    ProducerSettings(system, new ByteArraySerializer, new SystemEventSerializer)
      .withBootstrapServers(kafkaProducerBootServers)
      .withProperty(SaslConfigs.SASL_JAAS_CONFIG, kafkaSaslJaasConfig)
      .withProperty(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256")
      .withProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")

  def definedSource:
  Source[CommittableMessage[Array[Byte], GetScottishEsAndOsDataRequest], Control] = {
    val source_topic = configHelper.config.getString("esandos.source_topic")
    log.info(s"Source topic is '$source_topic'")
    Consumer.committableSource(consumerSettings, Subscriptions.topics(source_topic))
  }

  def definedSink: String = {
    val sink_topic = configHelper.config.getString("esandos.sink_topic")
    log.info(s"Sink topic is '$sink_topic'")
    sink_topic
  }

  def definedSystemEventsTopic: String = {
    val sink_topic = configHelper.config.getString("esandos.system_events_topic")
    log.info(s"Sink topic is '$sink_topic'")
    sink_topic
  }

}
