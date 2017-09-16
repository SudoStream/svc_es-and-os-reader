package io.sudostream.esandosreader.dao

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import io.sudostream.esandosreader.config.ActorSystemWrapper
import io.sudostream.timetoteach.messages.scottish._
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonString}

import scala.concurrent.{ExecutionContextExecutor, Future}

class MongoDbEsAndOsReaderDao(mongoFindQueriesProxy: MongoFindQueriesProxy,
                              actorSystemWrapper: ActorSystemWrapper) extends EsAndOsReaderDao {
  implicit val system: ActorSystem = actorSystemWrapper.system
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = actorSystemWrapper.materializer
  val log: LoggingAdapter = system.log



  override def extractAllScottishEsAndOs: Future[ScottishEsAndOsData] = {
    val esAndOsFutureSeqMongoDocuments: Future[Seq[Document]] = mongoFindQueriesProxy.findAllEsAndOs

    val scottishEsAndOsMetadataFuture: Future[ScottishEsAndOsMetadata] = esAndOsFutureSeqMongoDocuments map {
      case esAndOsDocument: Document =>
        val esAndOsMetadata: ScottishEsAndOsMetadata = createScottishEsAndOsMetadata(esAndOsDocument)
        esAndOsMetadata
    } recover {
      case e: Exception => throw new RuntimeException("There was an issue processing the Es and" +
        " Os returned from the database: " + e.getMessage)
    }

    // TODO: Build ScottishEsAndOsData from the Mongo Documents
    //    Future.failed(new RuntimeException("Issue "))

//    val esAndOsSeqFuture = for { singleEAndO <- scottishEsAndOsMetadataFuture } yield singleEAndO
//
//     ScottishEsAndOsData(allExperiencesAndOutcomes = esAndOs)


  }

  def createScottishEsAndOsMetadata(esAndOsDocument: Document): ScottishEsAndOsMetadata = {
//  private def createEsAndOsMetadata(esAndOsDocument: Seq[Document] with Document) = {
    val experienceAndOutcomes: Seq[Document] = esAndOsDocument("experienceAndOutcome") match {
      case someEsAndOs: Seq[Document] => someEsAndOs
      case _ =>
        val errorMsg = s"Invalid Experience and Outcome format" +
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
    }

    val eAndOSentencesAndBulletPoints: Seq[(String, List[String])] =
      for {eAndO <- experienceAndOutcomes
           theEAndO: BsonString = eAndO("sentence") match {
             case someBsonString: BsonString => someBsonString
             case _ =>
               val errorMsg = s"Invalid sentence format which should be string" +
                 s" which came from ${experienceAndOutcomes.toString()}"
               log.error(errorMsg)
               throw new RuntimeException(errorMsg)
           }
           theEAndOBulletPoints: BsonArray = eAndO("bulletPoints") match {
             case someBsonArray: BsonArray => someBsonArray
             case _ =>
               val errorMsg = s"Invalid bullet points format which should be list of string" +
                 s" which came from ${experienceAndOutcomes.toString()}"
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

    val theCodes: List[String] = esAndOsDocument("codes") match {
      case someStringsList: List[String] => someStringsList
      case _ =>
        val errorMsg = s"Invalid codes format which should be list of string" +
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
    }

    val theCurriculumLevelsAsStrings: List[String] = esAndOsDocument("curriculumLevels") match {
      case someStringsList: List[String] => someStringsList
      case _ =>
        val errorMsg = s"Invalid Curriculum Levels format which should be list of string" +
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
    }

    val theCurriculumLevels =
      theCurriculumLevelsAsStrings map {
        level =>
          if ("EARLY" == level) ScottishCurriculumLevel.EARLY
          else if ("FIRST" == level) ScottishCurriculumLevel.FIRST
          else if ("SECOND" == level) ScottishCurriculumLevel.SECOND
          else if ("THIRD" == level) ScottishCurriculumLevel.THIRD
          else if ("FOURTH" == level) ScottishCurriculumLevel.FOURTH
          else {
            val errorMsg = s"Didn't recognise Scottish Curriculum Level '$level'" +
              s" which came from ${experienceAndOutcomes.toString()}"
            log.error(errorMsg)
            throw new RuntimeException(errorMsg)
          }
      }

    val theCurriculumAreaNameAsString: String = esAndOsDocument("curriculumAreaName").toString
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
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
      }

    val theEAndOSetNameAsString: Option[String] = esAndOsDocument("eAndOSetName") match {
      case isOption: Option[String] => isOption
      case _ =>
        val errorMsg = s"Invalid Set Name format which should be list of string" +
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
    }

    val theEAndOSetName: Option[ScottishEAndOSetName] =
      if (theEAndOSetNameAsString.isDefined) {
        if ("LANGUAGES__CLASSICAL_LANGUAGES" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.LANGUAGES__CLASSICAL_LANGUAGES)
        else if ("LANGUAGES__GAELIC_LEARNERS" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.LANGUAGES__GAELIC_LEARNERS)
        else if ("LANGUAGES__LITERACY_AND_ENGLISH" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.LANGUAGES__LITERACY_AND_ENGLISH)
        else if ("LANGUAGES__LITERACY_AND_GAIDHLIG" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.LANGUAGES__LITERACY_AND_GAIDHLIG)
        else if ("LANGUAGES__MODERN_LANGUAGES" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.LANGUAGES__MODERN_LANGUAGES)
        else if ("RELIGIOUS_AND_MORAL_EDUCATION" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.RELIGIOUS_AND_MORAL_EDUCATION)
        else if ("RELIGIOUS_EDUCATION_IN_ROMAN_CATHOLIC_SCHOOLS" == theEAndOSetNameAsString.get) Some(ScottishEAndOSetName.RELIGIOUS_EDUCATION_IN_ROMAN_CATHOLIC_SCHOOLS)
        else Option.empty
      } else {
        Option.empty
      }

    val theEAndOSetSectionName: String = esAndOsDocument("eAndOSetSectionName").toString
    val theEAndOSetSubSectionName: Option[String] = esAndOsDocument("eAndOSetSubSectionName") match {
      case isOption: Option[String] => isOption
      case _ =>
        val errorMsg = s"Invalid Set Sub Section Name format which should be list of string" +
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
    }
    val theEAndOSetSubSectionAuxiliaryText: Option[String] = esAndOsDocument("eAndOSetSubSectionAuxiliaryText") match {
      case isOption: Option[String] => isOption
      case _ =>
        val errorMsg = s"Invalid Set Sub Sectoin Aux Text format which should be list of string" +
          s" which came from ${experienceAndOutcomes.toString()}"
        log.error(errorMsg)
        throw new RuntimeException(errorMsg)
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