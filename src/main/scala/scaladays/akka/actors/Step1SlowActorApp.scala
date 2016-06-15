package scaladays.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object Step1SlowActorApp extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  class SlowEcho extends Actor {
    def receive = {
      case m => 
        Thread.sleep(100) // DONT EVER DO THIS!!!!!
        sender() ! "OK"
    }
  }
  
  case class Send(n: Int, batch: Int)
  class Sender(slow: ActorRef) extends Actor {
    var sending: Send = _
    def receive = {
      case Send(n, _) if n < 0 => // done.
        
      case send @ Send(n, batch) =>
        sending = send
        (1 to batch).foreach { slow ! _ }
        self ! send.copy(n = send.n - batch)
    }
  }
  
  val slow = system.actorOf(Props(new SlowEcho), "slow")
//  val sender = system.actorOf(Props(new Sender(slow)), "sender")
//  
//  sender ! Send(100000, batch = 1000)

  
  Source.repeat("Hello")
    .take(100000)
    .runWith(Sink.actorRefWithAck(slow, 
      onInitMessage = "INIT", 
      ackMessage = "OK", 
      onCompleteMessage = "DONE"))
  
  
}

/*
[info] metrics.akka.systems.default.dispatchers.akka_actor_default-dispatcher.actors.scaladays_akka_actors_Step0PingPongApp$Sender.mailbox-size
[info]              count = 876
[info]                min = 0
[info]                max = 2
[info]               mean = 0.50
[info]             stddev = 0.50
[info]             median = 1.00
[info]               75% <= 1.00
[info]               95% <= 1.00
[info]               98% <= 1.00
[info]               99% <= 1.00
[info]             99.9% <= 1.00
[info] metrics.akka.systems.default.dispatchers.akka_actor_default-dispatcher.actors.scaladays_akka_actors_Step0PingPongApp$SlowEcho.mailbox-size
[info]              count = 101337
[info]                min = 24
[info]                max = 100946
[info]               mean = 50104.24
[info]             stddev = 28895.73
[info]             median = 48622.00
[info]               75% <= 74372.00
[info]               95% <= 97268.00
[info]               98% <= 100175.00
[info]               99% <= 100676.00
[info]             99.9% <= 100946.00



...


[info] metrics.akka.systems.default.dispatchers.akka_actor_default-dispatcher.actors.scaladays_akka_actors_Step0PingPongApp$SlowEcho.processing-time
[info]              count = 287
[info]                min = 100068901
[info]                max = 105250826
[info]               mean = 103027342.11
[info]             stddev = 1461378.41
[info]             median = 103384984.00
[info]               75% <= 104135603.00
[info]               95% <= 104989744.00
[info]               98% <= 105163940.00
[info]               99% <= 105243010.00
[info]             99.9% <= 105250826.00
 */
