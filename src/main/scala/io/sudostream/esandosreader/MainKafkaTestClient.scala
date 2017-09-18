package io.sudostream.esandosreader

import java.time.Instant

import akka.actor.ActorSystem
import akka.event.Logging
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import io.sudostream.esandosreader.Main.actorSystemWrapper
import io.sudostream.timetoteach.kafka.serializing.GetScottishEsAndOsDataRequestSerializer
import io.sudostream.timetoteach.messages.scottish.GetScottishEsAndOsDataRequest
import io.sudostream.timetoteach.messages.systemwide.TimeToTeachApplication
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer

//object MainKafkaTestClient extends App {
//  implicit val theActorSystem: ActorSystem = actorSystemWrapper.system
//  val logger = Logging(theActorSystem, getClass)
//  implicit val executor = theActorSystem.dispatcher
//  implicit val materializer = actorSystemWrapper.materializer
//
//  val producerSettings = ProducerSettings(theActorSystem,
//    new ByteArraySerializer, new GetScottishEsAndOsDataRequestSerializer)
//    .withBootstrapServers("localhost:9092")
//
//  val request = GetScottishEsAndOsDataRequest(
//    requestFingerprint = java.util.UUID.randomUUID().toString,
//    requestingSystem = TimeToTeachApplication.TEST_UNIT,
//    requestingSystemExtraInfo = Some("Basic Kafka test client"),
//    requestingUsername = Some("andy"),
//    originalUTCTimeOfRequest = Instant.now().toEpochMilli
//  )
//
//  val done = Source(List(request))
//    .map { elem =>
//      new ProducerRecord[Array[Byte], GetScottishEsAndOsDataRequest]("UI_REQUEST", elem)
//    }
//    .runWith(Producer.plainSink(producerSettings))
//
//}
