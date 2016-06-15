package scaladays.akka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scaladays.akka.domain.Tweet
import scaladays.akka.http.MyJsonProtocol
import scaladays.akka.support.{MakingUpData, PrintlnSupport}

object Step2QuerySideApp extends App with MyJsonProtocol 
  with PrintlnSupport {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val pq = PersistenceQuery(system)
  val readJournal = pq.readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  
  val fid: Future[String] = readJournal.currentPersistenceIds().runWith(Sink.head)
  fid foreach { id =>
    val events: Source[EventEnvelope, NotUsed] = readJournal.eventsByPersistenceId(id)
    events.runWith(printlnSink(name = s"event($id)"))
  }
  
}
