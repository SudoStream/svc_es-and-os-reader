package io.sudostream.esandosreader.dao

import java.util.concurrent.TimeoutException

import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.timetoteach.messages.scottish._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import org.scalatest.AsyncFlatSpec

class MongoDbEsAndOsReaderDaoTest extends AsyncFlatSpec with MockitoSugar {
  private val actorSystemWrapper = new ActorSystemWrapper
  private val mongoFindQueries = new TestMongoFindQueriesProxyStub

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
