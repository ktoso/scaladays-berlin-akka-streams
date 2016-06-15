package scaladays.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scaladays.akka.http.MyJsonProtocol
import scaladays.akka.support.MakingUpData

object Step3WebSocketClientApp extends App 
  with MakingUpData 
  with MyJsonProtocol {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  // TODO ./bin/kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic names
  
  val req = WebSocketRequest(uri = "ws://127.0.0.1:8000/step3/ws")
  val clientFlow = Flow.fromSinkAndSource(Sink.foreach(println), Source.single(ws.TextMessage("")))
  Http().singleWebSocketRequest(req, clientFlow)

}
