package io.sudostream.esandosreader.dao

import io.sudostream.timetoteach.messages.scottish.{ScottishEsAndOsData, ScottishEsAndOsMetadata}
import org.mongodb.scala.Document

import scala.concurrent.Future

class MongoDbEsAndOsReaderDao(mongoFindQueriesProxy: MongoFindQueriesProxy) extends EsAndOsReaderDao {

  def createScottishEsAndOsMetadata(doc: Document): ScottishEsAndOsMetadata = ???

  override def extractAllScottishEsAndOs: Future[ScottishEsAndOsData] = {
    val esAndOsFutureSeqMongoDocuments: Future[Seq[Document]] = mongoFindQueriesProxy.findAllEsAndOs

    // TODO: Build ScottishEsAndOsData from the Mongo Documents
    null
  }

}