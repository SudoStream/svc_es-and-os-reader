package io.sudostream.esandosreader.dao

import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData

import scala.concurrent.Future

trait EsAndOsReaderDao {
  def extractAllScottishEsAndOs: Future[ScottishEsAndOsData]
}
