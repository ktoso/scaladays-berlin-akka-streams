package scaladays.akka.http

import akka.NotUsed
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.FramedEntityStreamingDirectives
import akka.stream.scaladsl.{Framing, Sink, Source}

import scaladays.akka.domain.Tweet
import scaladays.akka.kafka.KafkaTopics

trait Step3IncomingStreamRoutes extends Directives
  with FramedEntityStreamingDirectives /* TODO early preview, not merged yet */
  with MyJsonProtocol {

  // TODO !!! This is using a pending PR: https://github.com/akka/akka/pull/20778  !!!         
  
  // TODO explain that we could either keep the mat/ec here, or extract them

  def step3Routes =
    // TODO explain that this or implicit defs
    extractMaterializer { implicit mat =>
      extractExecutionContext { implicit ec =>
        
        // TODO: http POST 127.0.0.1:8000/step3/raw ContentType:application/json Accept:*/* < tweets.json
        pathPrefix("step3") {
          path("raw") {
            withoutIdle{
              
            }
            
            extractDataBytes { dataBytes =>
              val sumDataBytes = dataBytes.runFold(0) { case (len, chunk) => len + chunk.size }
              onComplete(sumDataBytes) { totalLength =>
                complete(s"Total length of data: $totalLength bytes")
              }
            }
          } ~
          path("framed") {
            entity(asSource[Tweet]) { statuses =>
              val sumScore = statuses.runFold(0) { case (sentimentScore, stat) => sentimentScore + stat.sentimentScore }
              onComplete(sumScore) { totalSentimentsScore => 
                complete(s"total sentiments score: $totalSentimentsScore")
              }
            }
          } ~
          path("stream") {
            //  curl 127.0.0.1:8000/step3/stream
            val tenRandomTweets = 
              Source.repeat(NotUsed).take(10).map(_ => Tweet.random)
            complete(tenRandomTweets) // TODO: Show ToResponseMarshalable trick
          }
        }
        
      }
    }

}

