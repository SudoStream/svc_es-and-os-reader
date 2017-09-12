package io.sudostream.esandosreader.dao

import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData

trait EsAndOsReaderDao {
  def extractAllScottishEsAndOs: ScottishEsAndOsData
}
