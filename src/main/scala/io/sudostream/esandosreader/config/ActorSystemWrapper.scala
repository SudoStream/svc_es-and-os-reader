package io.sudostream.esandosreader.config

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.sudostream.esandosreader.Main.configHelper

class ActorSystemWrapper {
  lazy val system = ActorSystem("es-and-os-reader-system", configHelper.config)
  implicit val actorSystem = system
  lazy val materializer = ActorMaterializer()
}
