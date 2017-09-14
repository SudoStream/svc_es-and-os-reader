package io.sudostream.esandosreader.dao

import com.mongodb.async.client.{FindIterable, MongoCollection => JMongoCollection}
import io.sudostream.timetoteach.messages.scottish._
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.{Document, FindObservable, MongoCollection, bson}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class MongoDbEsAndOsReaderDaoTest extends FlatSpec with MockFactory {
  val mongoDbConnectionWrapper: MongoDbConnectionWrapper = stub[MongoDbConnectionWrapper]
  val esAndOsCollectionStub: JMongoCollection[Document] = stub[JMongoCollection[Document]]

  // TODO: Build proper document version of this
  val testDocument: Document = Document("Hello" -> BsonString("there"))
  val todoEsAndOsDoc: FindObservable[Document] = FindObservable[Document](FindIterable[Document])

    "Extracting All Scottish Es And Os from Dao" should "return correctly formatted documents" in {
      // configure mongo connection behavior
      mongoDbConnectionWrapper.getEsAndOsCollection _ when() returns esAndOsCollectionStub.asInstanceOf[MongoCollection[Document]]
      esAndOsCollectionStub.find() when() returns todoEsAndOsDoc
      //

      val esAndOsDao: EsAndOsReaderDao = new MongoDbEsAndOsReaderDao(mongoDbConnectionWrapper)
      val allScottishEsAndOs: ScottishEsAndOsData = esAndOsDao.extractAllScottishEsAndOs

      assert(allScottishEsAndOs.allExperiencesAndOutcomes.size === stubExtractScottishEsAndOsData)
    }

  def stubExtractScottishEsAndOsData: ScottishEsAndOsData = {
    val experiencesAndOutcomes: List[ScottishEsAndOsMetadata] = List(
      ScottishEsAndOsMetadata(
        experienceAndOutcome = List(
          ScottishExperienceAndOutcome(
            sentence = "I have experienced the energy and excitement of presenting/performing for " +
              "audiences and being part of an audience for other people’s presentations/performances.",
            bulletPoints = List()
          )
        ),
        codes = List("EXA 0-01a", "EXA 1-01a", "EXA 2-01a"),
        curriculumLevels = List(ScottishCurriculumLevel.EARLY, ScottishCurriculumLevel.FIRST, ScottishCurriculumLevel.SECOND),
        curriculumAreaName = ScottishCurriculumAreaName.EXPRESSIVE_ARTS,
        eAndOSetName = Option.empty,
        eAndOSetSectionName = "Participation in performances and presentations",
        eAndOSetSubSectionName = Option.empty,
        eAndOSetSubSectionAuxiliaryText = Option.empty,
        responsibilityOfAllPractitioners = true
      ),
      ScottishEsAndOsMetadata(
        experienceAndOutcome = List(
          ScottishExperienceAndOutcome(
            sentence = "I have used the skills I have developed in the expressive arts to contribute to a " +
              "public presentation/performance.",
            bulletPoints = List()
          )
        ),
        codes = List("EXA 3-01a"),
        curriculumLevels = List(ScottishCurriculumLevel.THIRD),
        curriculumAreaName = ScottishCurriculumAreaName.EXPRESSIVE_ARTS,
        eAndOSetName = Option.empty,
        eAndOSetSectionName = "Participation in performances and presentations",
        eAndOSetSubSectionName = Option.empty,
        eAndOSetSubSectionAuxiliaryText = Option.empty,
        responsibilityOfAllPractitioners = true
      ),
      ScottishEsAndOsMetadata(
        experienceAndOutcome = List(
          ScottishExperienceAndOutcome(
            sentence = "In everyday activity and play, I explore and make choices to develop my learning and interests. " +
              "I am encouraged to use and share my experiences.",
            bulletPoints = List()
          )
        ),
        codes = List("HWB 0-19a"),
        curriculumLevels = List(ScottishCurriculumLevel.EARLY),
        curriculumAreaName = ScottishCurriculumAreaName.HEALTH_AND_WELLBEING,
        eAndOSetName = Option.empty,
        eAndOSetSectionName = "Planning for choices and changes",
        eAndOSetSubSectionName = Option.empty,
        eAndOSetSubSectionAuxiliaryText = Option.empty,
        responsibilityOfAllPractitioners = true
      )
    )

    ScottishEsAndOsData(
      allExperiencesAndOutcomes = experiencesAndOutcomes
    )
  }

}
