package scaladays.akka.domain

final case class SentimentAnalysis(n: Long, score: Long, averageScore: Double) {
  
  def updated(status: Tweet): SentimentAnalysis = 
    copy(n = n + 1, score = score + status.sentimentScore, Math.round(score.toDouble / n))

}
