package scaladays.akka.stream

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.duration._
import scaladays.akka.actors.PersistGreetingActor
import scaladays.akka.actors.PersistGreetingActor.WhoToGreet
import scaladays.akka.http.MyJsonProtocol
import scaladays.akka.support.MakingUpData

object Step4PersistenceQuery extends App
  with MakingUpData with MyJsonProtocol {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val greetPersister = system.actorOf(PersistGreetingActor.props("greetPersister"), "greetPersister")

  // TODO: sending data here ----------------------------------------------------------------------
  system.actorOf(Props(new Actor {
      context.system.scheduler.schedule(1.second, 10.seconds, self, "PING")
      
      override def receive: Receive = {
        case _ =>
          greetPersister ! WhoToGreet("Johan") // who had an accident last week :'-(
          greetPersister ! WhoToGreet("Patrik")
          greetPersister ! WhoToGreet("Endre")
          greetPersister ! WhoToGreet("Martynas")
          greetPersister ! WhoToGreet("ktoso")
      }
    }), "sender")
  
  
  // TODO: Query side starts here -----------------------------------------------------------------

  private val pq = PersistenceQuery(system)
  val journal = pq.readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  val ids: Source[String, NotUsed] = journal.allPersistenceIds()
  ids.runForeach { id =>
    journal.currentEventsByPersistenceId(id).runForeach { e =>
      println(s"ID($id), event = $e")
    }
  }   

}
