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
import scala.util.Try

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
        val seqOfScottishEsAndOsMetadata: Seq[Try[ScottishEsAndOsBySubSection]] =
          for {
            singleEAndODocAtSubSectionLevel <- esAndOs
            singleScottishEAndOMetadata: Try[ScottishEsAndOsBySubSection] = createScottishEsAndOsBySubSection(singleEAndODocAtSubSectionLevel)
          } yield singleScottishEAndOMetadata

        val failures = seqOfScottishEsAndOsMetadata.filter(singleTry => singleTry.isFailure)

        if (failures.nonEmpty) {
          val errorMsg = "Failed to correctly parse Es And Os from database"
          log.error(errorMsg)
          return Future.failed(new RuntimeException(errorMsg))
        } else {
          val esAndOs = seqOfScottishEsAndOsMetadata map { esAndOsTry => esAndOsTry.get }
          ScottishEsAndOsData(allExperiencesAndOutcomes = esAndOs.toList)
        }
    }
  }

  def convertToSentencesAndBulletPoints(sentencesAsBsonArray: BsonArray): List[ScottishExperienceAndOutcomeLine] = {
    {
      for {
        eAndOElem <- sentencesAsBsonArray
        eAndO = eAndOElem.asDocument()
        sentence = eAndO.getString("sentence").getValue
        theEAndOBulletPoints: BsonArray = eAndO.getArray("bulletPoints")
      } yield ScottishExperienceAndOutcomeLine(
        sentence,
        theEAndOBulletPoints.toArray.toList.map(_.toString)
      )
    }.toList
  }


  def convertBenchmarksToList(benchmarks: BsonArray): List[String] = {
    {
      for {
        benchmarkAsValue <- benchmarks
      } yield benchmarkAsValue.asString().getValue
    }.toList
  }

  def createScottishEsAndOsBySubSection(esAndOsDocument: Document): Try[ScottishEsAndOsBySubSection] = {
    Try {
      val experienceAndOutcomesAtSubsectionLevelBsonArray = esAndOsDocument.get[BsonArray]("allExperienceAndOutcomesAtTheSubSectionLevel")
        .getOrElse(throw new RuntimeException("Expected an array here " +
          "for 'experienceAndOutcome' in ${esAndOsDocument.toString()"))

      val experienceAndOutcomesAtSubsectionLevelBsonValues = experienceAndOutcomesAtSubsectionLevelBsonArray.getValues

      val allEsAndOsAtSubsectionLevel = {
        for {
          singleEAndOValue <- experienceAndOutcomesAtSubsectionLevelBsonValues
          singleEAndODoc = singleEAndOValue.asDocument()
          eAndOCode = singleEAndODoc.getString("code").getValue
          sentencesAsBsonArray = singleEAndODoc.getArray("sentences")
          lines: List[ScottishExperienceAndOutcomeLine] = convertToSentencesAndBulletPoints(sentencesAsBsonArray)
        } yield SingleScottishExperienceAndOutcome(
          code = eAndOCode,
          eAndOLines = lines
        )
      }.toList

      val associatedBenchmarks = {
        {
          for {
            benchmarks <- esAndOsDocument.get[BsonArray]("associatedBenchmarks")
            benchmark = convertBenchmarksToList(benchmarks)
          } yield benchmark
        }.toList
      }.flatten

      val theCurriculumLevelAsString = esAndOsDocument.get[BsonString]("curriculumLevel").get.asString().getValue
      val theCurriculumLevel = theCurriculumLevelAsString match {
        case "EARLY" => ScottishCurriculumLevel.EARLY
        case "FIRST" => ScottishCurriculumLevel.FIRST
        case "SECOND" => ScottishCurriculumLevel.SECOND
        case "THIRD" => ScottishCurriculumLevel.THIRD
        case "FOURTH" => ScottishCurriculumLevel.FOURTH
      }

      val theCurriculumAreaNameAsString = esAndOsDocument.getString("curriculumAreaName")
      val theCurriculumAreaName: ScottishCurriculumAreaName =
        if ("Expressive Arts" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.EXPRESSIVE_ARTS
        else if ("Health And Wellbeing" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.HEALTH_AND_WELLBEING
        else if ("Languages" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.LANGUAGES
        else if ("Mathematics" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.MATHEMATICS
        else if ("Religion And Moral Education" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.RELIGION_AND_MORAL_EDUCATION
        else if ("Sciences" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.SCIENCES
        else if ("Social Studies" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.SOCIAL_STUDIES
        else if ("Technologies" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.TECHNOLOGIES
        else if ("Literacy" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.LITERACY
        else if ("Numeracy" == theCurriculumAreaNameAsString) ScottishCurriculumAreaName.NUMERACY
        else {
          val errorMsg = s"Didn't recognise Scottish Curriculum Area Name '$theCurriculumAreaNameAsString'" +
            s" which came from ${esAndOsDocument.toString()}"
          log.error(errorMsg)
          throw new RuntimeException(errorMsg)
        }

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

      ScottishEsAndOsBySubSection(
        allExperienceAndOutcomesAtTheSubSectionLevel = allEsAndOsAtSubsectionLevel,
        scottishCurriculumLevel = theCurriculumLevel,
        associatedBenchmarks = associatedBenchmarks,
        curriculumAreaName = theCurriculumAreaName,
        eAndOSetSectionName = theEAndOSetSectionName,
        eAndOSetSubSectionName = theEAndOSetSubSectionName,
        eAndOSetSubSectionAuxiliaryText = theEAndOSetSubSectionAuxiliaryText,
        responsibilityOfAllPractitioners = theResponsibilityOfAllPractitioners
      )
    }
  }


  ////////////////////////


  override def extractAllScottishBenchmarks: Future[ScottishBenchmarksData] = {
    val benchmarksFutureSeqMongoDocuments: Future[Seq[Document]] = mongoFindQueriesProxy.findAllBenchmarks
    benchmarksFutureSeqMongoDocuments.map { benchmarksInDocSequence =>

      val benchmarksWrappers = {
        for {
          benchmarkDoc <- benchmarksInDocSequence

          maybeEandoCodes = benchmarkDoc.get[BsonArray]("eandoCodes")
          theCodes = convertMaybeBsonArrayToListOfStrings(maybeEandoCodes)

          maybeBsonStringLevel = benchmarkDoc.get[BsonString]("level")
          maybeLevel = convertMaybeBsonStringToScottishCurriculumLevel(maybeBsonStringLevel)
          if maybeLevel.isDefined

          maybeBenchmarks = benchmarkDoc.get[BsonArray]("benchmarks")
          theBenchmarks = convertMaybeBsonArrayToListOfStrings(maybeBenchmarks)
        } yield ScottishBenchmarksWrapper(theCodes, maybeLevel.get, theBenchmarks)
      }.toList

      ScottishBenchmarksData(benchmarksWrappers)
    }
  }

  def convertMaybeBsonStringToScottishCurriculumLevel(maybeLevel: Option[BsonString]): Option[ScottishCurriculumLevel] = {
    log.debug(s"maybe level = ${maybeLevel.toString}")
    maybeLevel match {
      case Some(level) =>
        level.getValue match {
          case "EARLY" => Some(ScottishCurriculumLevel.EARLY)
          case "FIRST" => Some(ScottishCurriculumLevel.FIRST)
          case "SECOND" => Some(ScottishCurriculumLevel.SECOND)
          case "THIRD" => Some(ScottishCurriculumLevel.THIRD)
          case "FOURTH" => Some(ScottishCurriculumLevel.FOURTH)
          case somethingElse =>
            log.warning(s"Value $somethingElse is not valid currciulum level")
            None
        }
      case None => None
    }
  }

  def convertMaybeBsonArrayToListOfStrings(maybeEandoCodes: Option[BsonArray]): List[String] = {
    log.debug(s"maybeCodes = ${maybeEandoCodes.toString}")
    if (maybeEandoCodes.isDefined) {
      val eandoCodesArray = maybeEandoCodes.get
      log.debug(s"the codes = ${eandoCodesArray.toString}")
      (for {
        elem <- eandoCodesArray.getValues
      } yield elem.asString().toString).toList
    } else {
      Nil
    }
  }

}
