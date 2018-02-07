package io.sudostream.esandosreader.dao

import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.timetoteach.messages.scottish._
import org.bson.BsonArray
import org.mongodb.scala.bson.BsonString
import org.scalatest.AsyncFlatSpec
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoDbEsAndOsReaderDaoTest extends AsyncFlatSpec with MockitoSugar {
  System.setProperty("KAFKA_SASL_JASS_USERNAME", "user123")
  System.setProperty("KAFKA_SASL_JASS_PASSWORD", "pass123")
  private val actorSystemWrapper = new ActorSystemWrapper
  private val mongoFindQueries = new MongoFindQueriesProxyStub
  private val mongoFindQueriesWithError = new MongoFindQueriesProxyStubWithError

  "Extracting All Scottish Es And Os from Dao" should "return correctly formatted documents" in {
    val esAndOsDao: EsAndOsReaderDao = new MongoDbEsAndOsReaderDao(mongoFindQueries, actorSystemWrapper)
    val allScottishEsAndOsFuture: Future[ScottishEsAndOsData] = esAndOsDao.extractAllScottishEsAndOs

    allScottishEsAndOsFuture map {
      esAndOsData: ScottishEsAndOsData =>
        val esAndOs = stubExtractScottishEsAndOsData.allExperiencesAndOutcomes
        println(s"Es and Os created looks like : ${esAndOs.toString}")
        assert(esAndOsData.allExperiencesAndOutcomes.size === esAndOs.size)
    }

  }

  // TODO: Fix the code such that the following test fails rather than spins
  //  "Extracting All Scottish Es And Os from Dao with error" should "be reported clearly" in {
  //      val esAndOsDao: EsAndOsReaderDao = new MongoDbEsAndOsReaderDao(mongoFindQueriesWithError, actorSystemWrapper)
  //      val allScottishEsAndOsFuture: Future[ScottishEsAndOsData] = esAndOsDao.extractAllScottishEsAndOs
  //
  //      allScottishEsAndOsFuture onFailure {
  //        case ex => println(s"OOOOHH DEAR: $ex")
  //      }
  //
  //      allScottishEsAndOsFuture map {
  //        esAndOsData: ScottishEsAndOsData =>
  //          val esAndOs = stubExtractScottishEsAndOsData.allExperiencesAndOutcomes
  //          println(s"Es and Os created looks like : ${esAndOs.toString}")
  //          assert(esAndOsData.allExperiencesAndOutcomes.size === esAndOs.size)
  //      }
  //  }


  def stubExtractScottishEsAndOsData: ScottishEsAndOsData = {
    val experiencesAndOutcomes: List[ScottishEsAndOsBySubSection] = List(
      ScottishEsAndOsBySubSection(
        allExperienceAndOutcomesAtTheSubSectionLevel = List(
          SingleScottishExperienceAndOutcome(
            code = "EXA 0-01a",
            eAndOLines = List(
              ScottishExperienceAndOutcomeLine(sentence = "I have experienced the energy and excitement of presenting/performing for " +
                "audiences and being part of an audience for other people’s presentations/performances.",
                bulletPoints = List()
              )
            )
          )
        ),
        scottishCurriculumLevel = ScottishCurriculumLevel.EARLY,
        associatedBenchmarks = Nil,
        curriculumAreaName = ScottishCurriculumAreaName.EXPRESSIVE_ARTS,
        eAndOSetSectionName = "Participation in performances and presentations",
        eAndOSetSubSectionName = Option.empty,
        eAndOSetSubSectionAuxiliaryText = Option.empty,
        responsibilityOfAllPractitioners = true
      ),
      ScottishEsAndOsBySubSection(
        allExperienceAndOutcomesAtTheSubSectionLevel = List(
          SingleScottishExperienceAndOutcome(
            code = "EXA 3-01a",
            eAndOLines = List(
              ScottishExperienceAndOutcomeLine(
                sentence = "I have used the skills I have developed in the expressive arts to contribute to a " +
                  "public presentation/performance.",
                bulletPoints = List())
            )
          )
        ),
        scottishCurriculumLevel = ScottishCurriculumLevel.THIRD,
        associatedBenchmarks = Nil,
        curriculumAreaName = ScottishCurriculumAreaName.EXPRESSIVE_ARTS,
        eAndOSetSectionName = "Participation in performances and presentations",
        eAndOSetSubSectionName = Option.empty,
        eAndOSetSubSectionAuxiliaryText = Option.empty,
        responsibilityOfAllPractitioners = true
      ),
      ScottishEsAndOsBySubSection(
        allExperienceAndOutcomesAtTheSubSectionLevel = List(
          SingleScottishExperienceAndOutcome(
            code = "HWB 0-19a",
            eAndOLines = List(
              ScottishExperienceAndOutcomeLine(
                sentence = "In everyday activity and play, I explore and make choices to develop my learning and interests. " +
                  "I am encouraged to use and share my experiences.",
                bulletPoints = List())
            )
          )
        ),
        scottishCurriculumLevel = ScottishCurriculumLevel.EARLY,
        associatedBenchmarks = Nil,
        curriculumAreaName = ScottishCurriculumAreaName.HEALTH_AND_WELLBEING,
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


  ////

  private def createBsonArrayOfStrings(): BsonArray = {
    val someStringsInBsonArray: BsonArray = new BsonArray()
    someStringsInBsonArray.add(BsonString("hello"))
    someStringsInBsonArray.add(BsonString("there"))
    someStringsInBsonArray.add(BsonString("world"))
    someStringsInBsonArray
  }

  "Converting a defined list of BsonArray strings" should "return a list of the same size" in {
    val esAndOsDao: MongoDbEsAndOsReaderDao = new MongoDbEsAndOsReaderDao(mongoFindQueries, actorSystemWrapper)

    val stringsInList = esAndOsDao.convertMaybeBsonArrayToListOfStrings(Some(createBsonArrayOfStrings()))

    assert(stringsInList.size === 3)
  }

  "Converting a BsonString of 'FIRST'" should "convert to the same currciulum level" in {
    val esAndOsDao: MongoDbEsAndOsReaderDao = new MongoDbEsAndOsReaderDao(mongoFindQueries, actorSystemWrapper)

    val maybeCurriculumLevel = esAndOsDao.convertMaybeBsonStringToScottishCurriculumLevel(Some(BsonString("FIRST")))

    assert(maybeCurriculumLevel.isDefined)
    assert(maybeCurriculumLevel.get === ScottishCurriculumLevel.FIRST)
  }

  "Converting a BsonString of 'TYPO'" should "convert to None" in {
    val esAndOsDao: MongoDbEsAndOsReaderDao = new MongoDbEsAndOsReaderDao(mongoFindQueries, actorSystemWrapper)

    val maybeCurriculumLevel = esAndOsDao.convertMaybeBsonStringToScottishCurriculumLevel(Some(BsonString("TYPO")))

    assert(maybeCurriculumLevel.isEmpty)
  }


}
