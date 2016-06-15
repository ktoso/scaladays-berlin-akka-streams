package scaladays.akka.http

import akka.http.scaladsl.marshallers.sprayjson.ExtendedSprayJsonSupport
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.FramedEntityStreamingDirectives
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.ExecutionContext
import scaladays.akka.kafka.KafkaTopics

trait Step3WebsocketRoutes extends Directives with FramedEntityStreamingDirectives
  with ExtendedSprayJsonSupport with MyJsonProtocol {
  implicit def ec: ExecutionContext
  implicit def materializer: Materializer
  def kafkaTopics: KafkaTopics

  lazy val namesFromKafka = 
      kafkaTopics.namesSource.map { comittable =>
        comittable.committableOffset.commitScaladsl()
        comittable.value
      }

  lazy val namesAsWebSocketMessages =
    namesFromKafka.map(ws.TextMessage(_))
  
  def step3WsRoutes =
    path("step3" / "ws") {
      val source = Flow.fromSinkAndSource(Sink.ignore, namesAsWebSocketMessages)
      handleWebSocketMessages(source)
    }
  
}
