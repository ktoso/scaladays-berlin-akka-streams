package scaladays.akka.stream

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString

import scaladays.akka.domain.Tweet
import scaladays.akka.http.MyJsonProtocol
import scaladays.akka.support.MakingUpData

object Step2Hundredify extends App
  with MakingUpData with MyJsonProtocol {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val n = 10000

  Source.single(Tweet.random)
    .map(_.from)
    .mapConcat(name => Vector.fill(100)(name)) // TODO: show impl
//    .via(RepeatNTimes(n = 100))
    .runFold(0) { case (acc, t) => acc + 1 }
    .map { count =>
      println("Total number of elements was: " + count)
      system.terminate()
    }
  
  // TODO show test
  
}
