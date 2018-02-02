package io.sudostream.esandosreader.dao

import org.mongodb.scala.Document

import scala.concurrent.Future

trait MongoFindQueriesProxy {
  def findAllEsAndOs : Future[Seq[Document]]
  def findAllBenchmarks : Future[Seq[Document]]
}
