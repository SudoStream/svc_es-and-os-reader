package io.sudostream.esandosreader

import akka.actor.ActorSystem


object Main extends App with Service {

}


trait Service {
  
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  val logger: LoggingAdapter

  def config: Config

}
