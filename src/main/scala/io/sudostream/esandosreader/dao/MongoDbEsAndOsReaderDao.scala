package io.sudostream.esandosreader.dao

import io.sudostream.esandosreader.api.kafka.TODO_MoveToTest_ExtractScottishEsAndOsData
import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData

class MongoDbEsAndOsReaderDao extends EsAndOsReaderDao with TODO_MoveToTest_ExtractScottishEsAndOsData {
  override def extractAllScottishEsAndOs: ScottishEsAndOsData = {
    stubExtractScottishEsAndOsData
  }
}
