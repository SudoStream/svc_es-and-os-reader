package io.sudostream.esandosreader.dao

import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonString}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoFindQueriesProxyStub extends MongoFindQueriesProxy {

  override def findAllEsAndOs: Future[Seq[Document]] = {
    val esAndOs1 = Document(
      "allExperienceAndOutcomesAtTheSubSectionLevel" -> BsonArray(
        Document(
          "code" -> "EXA 0-01a",
          "sentences" -> BsonArray(
            Document(
              "sentence" -> BsonString("I have experienced the energy and excitement of presenting/performing for " +
                "audiences and being part of an audience for other people’s presentations/performances."),
              "bulletPoints" -> BsonArray()
            )
          )
        )
      ),
      "associatedBenchmarks" -> BsonArray(),
      "curriculumLevel" -> BsonString("EARLY"),
      "curriculumAreaName" -> BsonString("Expressive Arts"),
      "eAndOSetSectionName" -> BsonString("Participation in performances and presentations"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    val esAndOs2 = Document(
      "allExperienceAndOutcomesAtTheSubSectionLevel" -> BsonArray(
        Document(
          "code" -> "EXA 3-01a",
          "sentences" -> BsonArray(
            Document(
              "sentence" -> BsonString("I have used the skills I have developed in the expressive arts to contribute to a " +
                "public presentation/performance."),
              "bulletPoints" -> BsonArray()
            )
          )
        )
      ),
      "associatedBenchmarks" -> BsonArray(),
      "curriculumLevel" -> BsonString("THIRD"),
      "curriculumAreaName" -> BsonString("Expressive Arts"),
      "eAndOSetSectionName" -> BsonString("Participation in performances and presentations"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    val esAndOs3 = Document(
      "allExperienceAndOutcomesAtTheSubSectionLevel" -> BsonArray(
        Document(
          "code" -> "HWB 0-19a",
          "sentences" -> BsonArray(
            Document(
              "sentence" -> BsonString("In everyday activity and play, I explore and make choices to develop my " +
                "learning and interests. I am encouraged to use and share my experiences."),
              "bulletPoints" -> BsonArray()
            )
          )
        )
      ),
      "curriculumLevel" -> BsonString("EARLY"),
      "curriculumAreaName" -> BsonString("Health And Wellbeing"),
      "eAndOSetSectionName" -> BsonString("Planning for choices and changes"),
      "eAndOSetSubSectionName" -> None,
      "eAndOSetSubSectionAuxiliaryText" -> None,
      "responsibilityOfAllPractitioners" -> BsonBoolean(true)
    )

    Future {
      List(esAndOs1, esAndOs2, esAndOs3)
    }
  }

  override def findAllBenchmarks: Future[Seq[Document]] = ???

}
