package scaladays.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Future
import scaladays.akka.http.Routes
import scaladays.akka.kafka.KafkaTopics

object HttpApp extends App with Routes {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  lazy val kafkaTopics = new KafkaTopics(system)
  
  val binding: Future[ServerBinding] = 
    Http().bindAndHandle(routes, "0.0.0.0", 8000)
  
  println("Bound to: 0.0.0.0:8000")
  
//  kafkaTopics.namesSource
//    .mapAsync(1)(x => x.committableOffset.commitScaladsl().map(_ => x.value))
//    .runWith(Sink.foreach(println))
  
  
//  readLine("Press ENTER to quit...")
//  binding map { b =>
//    b.unbind().flatMap(_ => system.terminate())
//  }
}
