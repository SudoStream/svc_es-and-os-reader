package io.sudostream.esandosreader.dao

import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData
import org.mongodb.scala.{Document, FindObservable, MongoCollection}

class MongoDbEsAndOsReaderDao(mongoDbConnectionWrapper: MongoDbConnectionWrapper) extends EsAndOsReaderDao {

  val esAndOsCollection: MongoCollection[Document] = mongoDbConnectionWrapper.getEsAndOsCollection

  override def extractAllScottishEsAndOs: ScottishEsAndOsData = {
    val esAndOs: FindObservable[Document] = esAndOsCollection.find(Document())


    // TODO: Impl
    return null
  }

}
