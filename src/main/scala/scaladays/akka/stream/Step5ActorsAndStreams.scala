package scaladays.akka.stream

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Status}
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}

import scala.concurrent.Future

object Step5ActorsAndStreams extends App {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val printer = system.actorOf(Props(new Actor {
    override def receive: Receive = {
      case msg => println(s"printer: $msg")
    }
  }), "printer")
  
  // TODO <<<<<< LOOP BACK TO THE `Step1SlowActorApp` >>>>>>
  
  
  //////////////////////////////////////////////////////////////////////
  //             Source.actorRef                                      //
  //////////////////////////////////////////////////////////////////////
  
  // TODO where did the Ref come from?
  val inputActorRef: ActorRef =
    Source.actorRef[String](bufferSize = 100, overflowStrategy = OverflowStrategy.dropTail)
      .map(_ + "!!!")
      .to(Sink.actorRef(printer, "StreamCompleted")) // TODO notice Sink.actorRef
      .run()
  
  inputActorRef ! "Hello"
  inputActorRef ! "Hi"
  inputActorRef ! Status.Success()

  //////////////////////////////////////////////////////////////////////
   
  
  
  //////////////////////////////////////////////////////////////////////
  //             Source.queue                                         //
  //////////////////////////////////////////////////////////////////////
  
  // TODO where did the Ref come from?
  val inputQueue: SourceQueueWithComplete[String] =
    Source.queue[String](bufferSize = 100, overflowStrategy = OverflowStrategy.dropTail)
      .map(_ + "???")
      .to(Sink.actorRef(printer, "StreamCompleted"))
      .run()
  
  // TODO show types here
  val r1: Future[QueueOfferResult] = inputQueue.offer("Who are you")
  val r2 = inputQueue.offer("Where am I")
  inputQueue.watchCompletion().onComplete { _ => println("Complete, yay!") }

  //////////////////////////////////////////////////////////////////////
   
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////
  //             Sink.actorRefWithAck                                 //
  //////////////////////////////////////////////////////////////////////
  
  
  val printerWithAck = system.actorOf(Props(new Actor {
      override def receive: Receive = {
        case msg => 
          println(s"ack-printer: $msg")
          sender() ! "ACK"
      }
    }), "ack-printer")
  
  
  
  // TODO where did the Ref come from?
  val run =
    Source.single("Hello")
      .map(_ + "?!")
      .to(Sink.actorRefWithAck(printerWithAck, 
        onInitMessage     = "INIT", 
        ackMessage        = "ACK", 
        onCompleteMessage = "COMPLETE"))
      .run()
  
  //////////////////////////////////////////////////////////////////////
  
  Thread.sleep(1000)
  system.terminate()

}
