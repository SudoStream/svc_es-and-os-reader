package io.sudostream.esandosreader.dao

import org.mongodb.scala.{Document, MongoCollection}

trait MongoDbConnectionWrapper {

  def getEsAndOsCollection: MongoCollection[Document]

  def getBenchmarksCollection: MongoCollection[Document]

}
