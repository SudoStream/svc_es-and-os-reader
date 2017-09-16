package io.sudostream.esandosreader.dao

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.timetoteach.messages.scottish._
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonString}

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContextExecutor, Future}

class MongoDbEsAndOsReaderDao(mongoFindQueriesProxy: MongoFindQueriesProxy,
                              actorSystemWrapper: ActorSystemWrapper) extends EsAndOsReaderDao {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log: LoggingAdapter = system.log


  override def extractAllScottishEsAndOs: Future[ScottishEsAndOsData] = {
    val esAndOsFutureSeqMongoDocuments: Future[Seq[Document]] = mongoFindQueriesProxy.findAllEsAndOs

    esAndOsFutureSeqMongoDocuments map {
      esAndOs =>
        val seqOfScottishEsAndOsMetadata: Seq[ScottishEsAndOsMetadata] =
          for {
            singleEAndODoc <- esAndOs
            singleScottishEAndOMetadata: ScottishEsAndOsMetadata = createScottishEsAndOsMetadata(singleEAndODoc)
          } yield singleScottishEAndOMetadata

        ScottishEsAndOsData(allExperiencesAndOutcomes = seqOfScottishEsAndOsMetadata.toList)
    }


  }

  def createScottishEsAndOsMetadata(esAndOsDocument: Document): ScottishEsAndOsMetadata = {
    val experienceAndOutcomesBsonArray = esAndOsDocument.get[BsonArray]("experienceAndOutcome")
      .getOrElse(throw new RuntimeException("Expected an array here " +
        "for 'experienceAndOutcome' in ${esAndOsDocument.toString()"))

    val experienceAndOutcomes = experienceAndOutcomesBsonArray.getValues

    val eAndOSentencesAndBulletPoints: Seq[(String, List[String])] =
      for {
        eAndOElem <- experienceAndOutcomes
        eAndO = eAndOElem.asDocument()

        theEAndO: BsonString = eAndO.getString("sentence") match {
          case someString: BsonString => someString
          case _ =>
            val errorMsg = s"Invalid sentence format which should be string" +
              s" which came from ${esAndOsDocument.toString()}"
            log.error(errorMsg)
            throw new RuntimeException(errorMsg)
        }

        theEAndOBulletPoints: BsonArray = eAndO("bulletPoints") match {
          case someBsonArray: BsonArray => someBsonArray
          case _ =>
            val errorMsg = s"Invalid bullet points format which should be list of string" +
              s" which came from ${esAndOsDocument.toString()}"
            log.error(errorMsg)
            throw new RuntimeException(errorMsg)
        }

      } yield (theEAndO.toString, theEAndOBulletPoints.toArray.toList.map(_.toString))

    val scottishExperienceAndOutcomesPrepped =
      for {entry <- eAndOSentencesAndBulletPoints
           theSentence = entry._1
           theBulletPoints = entry._2
      } yield ScottishExperienceAndOutcome(
        sentence = theSentence,
        bulletPoints = theBulletPoints
      )

    val theCodesBsonArray = esAndOsDocument.get[BsonArray]("codes")
      .getOrElse(throw new RuntimeException("Expected an array here " +
        "for 'codes' in ${esAndOsDocument.toString()"))

    val theCodes: List[String] =
      (for {
        elem <- theCodesBsonArray.getValues
      } yield elem.asString().toString).toList


    val theCurriculumLevelsAsBsonArray = esAndOsDocument.get[BsonArray]("curriculumLevels")
      .getOrElse(throw new RuntimeException("Expected an array here " +
        "for 'codes' in ${esAndOsDocument.toString()"))
    val theCurriculumLevelsAsStrings: List[String] =
      (for {
        elem <- theCurriculumLevelsAsBsonArray.getValues
        elemString = elem.asString()
      } yield elemString.getValue).toList


    val theCurriculumLevels =
      for {
        elem <- theCurriculumLevelsAsStrings
        level = if ("EARLY" == elem) ScottishCurriculumLevel.EARLY
        else if ("FIRST" == elem) ScottishCurriculumLevel.FIRST
        else if ("SECOND" == elem) ScottishCurriculumLevel.SECOND
        else if ("THIRD" == elem) ScottishCurriculumLevel.THIRD
        else if ("FOURTH" == elem) ScottishCurriculumLevel.FOURTH
        else {
          val errorMsg = s"Didn't recognise Scottish Curriculum Level '$elem'" +
            s" which came from ${esAndOsDocument.toString()}"
          log.error(errorMsg)
          throw new RuntimeException(errorMsg)
        }
      } yield level

    val theCurriculumAreaNameAsString = esAndOsDocument.getString("curriculumAreaName")
    val theCurriculumAreaName: ScottishCurriculumAreaName =
      if ("EXPRESSIVE_ARTS" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.EXPRESSIVE_ARTS
      else if ("HEALTH_AND_WELLBEING" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.HEALTH_AND_WELLBEING
      else if ("LANGUAGES" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.LANGUAGES
      else if ("MATHEMATICS" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.MATHEMATICS
      else if ("RELIGION_AND_MORAL_EDUCATION" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.RELIGION_AND_MORAL_EDUCATION
      else if ("SCIENCES" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.SCIENCES
      else if ("SOCIAL_STUDIES" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.SOCIAL_STUDIES
      else if ("TECHNOLOGIES" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.TECHNOLOGIES
      else {
        val errorMsg = s"Didn't recognise Scottish Curriculum Area Name '$theCurriculumAreaNameAsString'" +
          s" which came from ${esAndOsDocument.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
      }

    val theEAndOSetNameAsString = esAndOsDocument.getString("eAndOSetName")
    val theEAndOSetName: Option[ScottishEAndOSetName] =
      if ("LANGUAGES__CLASSICAL_LANGUAGES" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.LANGUAGES__CLASSICAL_LANGUAGES)
      else if ("LANGUAGES__GAELIC_LEARNERS" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.LANGUAGES__GAELIC_LEARNERS)
      else if ("LANGUAGES__LITERACY_AND_ENGLISH" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.LANGUAGES__LITERACY_AND_ENGLISH)
      else if ("LANGUAGES__LITERACY_AND_GAIDHLIG" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.LANGUAGES__LITERACY_AND_GAIDHLIG)
      else if ("LANGUAGES__MODERN_LANGUAGES" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.LANGUAGES__MODERN_LANGUAGES)
      else if ("RELIGIOUS_AND_MORAL_EDUCATION" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.RELIGIOUS_AND_MORAL_EDUCATION)
      else if ("RELIGIOUS_EDUCATION_IN_ROMAN_CATHOLIC_SCHOOLS" == theEAndOSetNameAsString) Some(ScottishEAndOSetName.RELIGIOUS_EDUCATION_IN_ROMAN_CATHOLIC_SCHOOLS)
      else Option.empty

    val theEAndOSetSectionName: String = esAndOsDocument.getString("eAndOSetSectionName")
    val theEAndOSetSubSectionName = esAndOsDocument.getString("eAndOSetSubSectionName") match {
      case theString => if (null == theString || theString.isEmpty) {
        Option.empty
      } else {
        Some(theString)
      }
      case _ => Option.empty
    }

    val theEAndOSetSubSectionAuxiliaryText: Option[String] = esAndOsDocument.getString("eAndOSetSubSectionAuxiliaryText") match {
      case theString => if (null == theString || theString.isEmpty) {
        Option.empty
      } else {
        Some(theString)
      }
      case _ => Option.empty
    }

    val theResponsibilityOfAllPractitioners: Boolean = esAndOsDocument.getBoolean("responsibilityOfAllPractitioners")

    ScottishEsAndOsMetadata(
      experienceAndOutcome = scottishExperienceAndOutcomesPrepped.toList,
      codes = theCodes,
      curriculumLevels = theCurriculumLevels,
      curriculumAreaName = theCurriculumAreaName,
      eAndOSetName = theEAndOSetName,
      eAndOSetSectionName = theEAndOSetSectionName,
      eAndOSetSubSectionName = theEAndOSetSubSectionName,
      eAndOSetSubSectionAuxiliaryText = theEAndOSetSubSectionAuxiliaryText,
      responsibilityOfAllPractitioners = theResponsibilityOfAllPractitioners
    )
  }
}
