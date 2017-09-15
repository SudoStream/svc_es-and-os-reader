package io.sudostream.esandosreader.dao

import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonNull, BsonString}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class TestMongoFindQueriesProxyStub extends MongoFindQueriesProxy {

  override def findAllEsAndOs: Future[Seq[Document]] = {
    val esAndOs1 = Document(
      "experienceAndOutcome" -> BsonArray(
        Document(
          "sentence" -> BsonString("I have experienced the energy and excitement of presenting/performing for " +
            "audiences and being part of an audience for other people’s presentations/performances."),
          "bulletPoints" -> BsonArray()
        )
      ),
      "codes" -> BsonArray(
        BsonString("EXA 0-01a"),
        BsonString("EXA 1-01a"),
        BsonString("EXA 2-01a")
      ),
      "curriculumLevels" -> BsonArray(
        BsonString("EARLY"),
        BsonString("FIRST"),
        BsonString("SECOND")
      ),
      "curriculumAreaName" -> BsonString("EXPRESSIVE_ARTS"),
      "eAndOSetName" -> None,
      "eAndOSetSectionName" -> BsonString("Participation in performances and presentations"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    Future {
      List(esAndOs1)
    }
  }

}
