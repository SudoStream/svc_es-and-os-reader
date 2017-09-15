package io.sudostream.esandosreader.dao

import org.mongodb.scala.{Document, FindObservable, MongoCollection}

import scala.concurrent.Future

class MongoFindQueriesImpl(mongoDbConnectionWrapper: MongoDbConnectionWrapper) extends MongoFindQueries {
  val esAndOsCollection: MongoCollection[Document] = mongoDbConnectionWrapper.getEsAndOsCollection

  override def findAllEsAndOs: Future[Seq[Document]] = {
    val esAndOsMongoDocuments: FindObservable[Document] = esAndOsCollection.find(Document())
    esAndOsMongoDocuments.toFuture()
  }
}
