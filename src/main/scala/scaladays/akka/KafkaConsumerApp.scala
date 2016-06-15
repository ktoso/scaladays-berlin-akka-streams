package scaladays.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Future
import scaladays.akka.http.Routes
import scaladays.akka.kafka.KafkaTopics

object KafkaConsumerApp extends App {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val kafkaTopics = new KafkaTopics(system)

  // TODO: Milestone 3 release
  
  val consumeStatuses = kafkaTopics.statusSource
    .mapAsync(1)(x => x.committableOffset.commitScaladsl().map(_ => x.value))
    .to(Sink.foreach(println))
  
  val control: Control = consumeStatuses.run()

  // explain materialized value
  control.shutdown() // close consumer
}
