package scaladays.akka.actors

import akka.actor.{Actor, Props}

import scaladays.akka.actors.PersistGreetingActor.{Greeting, WhoToGreet}


object SlowGreeterActor {
  def props = Props(new SlowGreeterActor)
  
  final case class WhoToGreet(who: String)
  final case class Greeting(message: String)
}

class SlowGreeterActor extends Actor {
  override def receive: Receive = {
    case WhoToGreet(name) =>
      beSlow(name)
      sender() ! Greeting(s"Hello, $name")
  }

  def beSlow(name: String): Unit = {
    // FIXME: BAD BAD BAD, DON'T DO THIS
    Thread.sleep(1000)
  }
}
