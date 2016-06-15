package scaladays.akka.http

import akka.http.scaladsl.marshallers.sprayjson.{ExtendedSprayJsonSupport, SprayJsonSupport}
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.server.{JsonEntityFramingSupport, JsonSourceRenderingMode}
import akka.util.ByteString
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future
import scaladays.akka.domain.{SentimentAnalysis, Tweet}

trait MyJsonProtocol extends ExtendedSprayJsonSupport with DefaultJsonProtocol {

  implicit val jsonFramingMode = JsonEntityFramingSupport.bracketCountingJsonFraming(Int.MaxValue)
  
  implicit val renderingMode = JsonSourceRenderingMode.LineByLine
  
  implicit val StatusFormat = jsonFormat3(Tweet.apply)
  implicit val SentimentAnalysisFormat = jsonFormat3(SentimentAnalysis.apply)

  // convenience helpers
  
  implicit def js2byteString[T](implicit m: RootJsonFormat[T]): Marshaller[T, ByteString] =
    js2string(m).map(ByteString(_))
  
  implicit def js2string[T](implicit m: RootJsonFormat[T]): Marshaller[T, String] =
    Marshaller[T, String] { implicit ec => t => 
      Future.successful(Marshalling.Opaque(() ⇒ m.write(t).compactPrint) :: Nil)
    }
  
}
