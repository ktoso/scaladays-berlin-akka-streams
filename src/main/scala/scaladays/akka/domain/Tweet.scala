package scaladays.akka.domain

import scaladays.akka.support.MakingUpData

object Tweet extends MakingUpData {
  def random = Tweet(shortName(), lipsum(), int())
}

case class Tweet(from: String, text: String, sentimentScore: Int)
