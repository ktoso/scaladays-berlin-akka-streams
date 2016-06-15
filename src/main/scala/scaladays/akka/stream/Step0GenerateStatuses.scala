package scaladays.akka.stream

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import scaladays.akka.domain.Tweet
import scaladays.akka.http.MyJsonProtocol
import scaladays.akka.support.MakingUpData

object Step0GenerateStatuses extends App
  with MakingUpData with MyJsonProtocol {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val n = 10000

  Source.repeat(NotUsed).take(n)
    .map(_ => Tweet.random)
    .mapAsync(1)(t => Marshal(t).to[ByteString])
    .intersperse(ByteString("\n"))
    .runWith(FileIO.toPath(new File("tweets.json").toPath))
    .onComplete { res =>
      println(s"Generated $n tweets. ($res)")
      system.terminate()
    }

}
