package io.sudostream.esandosreader.dao

import org.mongodb.scala.{Document, FindObservable, MongoCollection}

import scala.concurrent.Future

class MongoFindQueriesImpl(mongoDbConnectionWrapper: MongoDbConnectionWrapper) extends MongoFindQueriesProxy {
  val esAndOsCollection: MongoCollection[Document] = mongoDbConnectionWrapper.getEsAndOsCollection
  val benchmarksCollection: MongoCollection[Document] = mongoDbConnectionWrapper.getBenchmarksCollection

  override def findAllEsAndOs: Future[Seq[Document]] = {
    val esAndOsMongoDocuments: FindObservable[Document] = esAndOsCollection.find(Document())
    esAndOsMongoDocuments.toFuture()
  }

  override def findAllBenchmarks: Future[Seq[Document]] = {
    val benchmarksDocuments: FindObservable[Document] = benchmarksCollection.find(Document())
    benchmarksDocuments.toFuture()
  }

}
