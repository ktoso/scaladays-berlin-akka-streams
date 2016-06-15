package scaladays.akka.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

import scaladays.akka.actors.PersistGreetingActor.{Greeting, WhoToGreet}


object PersistGreetingActor {
  def props(name: String) = Props(new PersistGreetingActor(name))
  
  final case class WhoToGreet(who: String)
  final case class Greeting(message: String)
}

class PersistGreetingActor(name: String) extends PersistentActor with ActorLogging {
  override def persistenceId: String = name
  
  override def receiveCommand: Receive = {
    case WhoToGreet(who) =>
      
      persist(Greeting(who)) { greeting =>
        log.info("Stored greeting: " + greeting)
        sender() ! greeting
      }
  }

  override def receiveRecover: Receive = {
    case msg => log.info("recovering: " + msg)
  }
  
  def beSlow(name: String): Unit = {
    // FIXME: BAD BAD BAD, DON'T DO THIS
    Thread.sleep(1000)
  }
  
}
