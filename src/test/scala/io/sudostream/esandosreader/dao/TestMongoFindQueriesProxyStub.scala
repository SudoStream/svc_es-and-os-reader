package io.sudostream.esandosreader.dao

import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonString}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
      "curriculumAreaName" -> BsonString("Expressive Arts"),
      "eAndOSetName" -> None,
      "eAndOSetSectionName" -> BsonString("Participation in performances and presentations"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    val esAndOs2 = Document(
      "experienceAndOutcome" -> BsonArray(
        Document(
          "sentence" -> BsonString("I have used the skills I have developed in the expressive arts to contribute to a " +
            "public presentation/performance."),
          "bulletPoints" -> BsonArray()
        )
      ),
      "codes" -> BsonArray(
        BsonString("EXA 3-01a")
      ),
      "curriculumLevels" -> BsonArray(
        BsonString("THIRD")
      ),
      "curriculumAreaName" -> BsonString("Expressive Arts"),
      "eAndOSetName" -> None,
      "eAndOSetSectionName" -> BsonString("Participation in performances and presentations"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    val esAndOs3 = Document(
      "experienceAndOutcome" -> BsonArray(
        Document(
          "sentence" -> BsonString("In everyday activity and play, I explore and make choices to develop my " +
            "learning and interests. I am encouraged to use and share my experiences."),
          "bulletPoints" -> BsonArray()
        )
      ),
      "codes" -> BsonArray(
        BsonString("HWB 0-19a")
      ),
      "curriculumLevels" -> BsonArray(
        BsonString("EARLY")
      ),
      "curriculumAreaName" -> BsonString("Health And Wellbeing"),
      "eAndOSetName" -> None,
      "eAndOSetSectionName" -> BsonString("Planning for choices and changes"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    Future {
      List(esAndOs1, esAndOs2, esAndOs3)
    }
  }

}
