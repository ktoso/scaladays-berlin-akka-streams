package scaladays.akka.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class RepeatNTimesSpec extends WordSpec with Matchers with BeforeAndAfterAll 
  with ScalaFutures {
  
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  "RepeatNTimesSpec" must {
    "repeat 3 times" in {
      val xs = Source.single("x")
        .via(RepeatNTimes(3))
        .runWith(Sink.seq).futureValue
      
      xs should === (List("x", "x", "x"))
    }
    
    "repeat 3 things 3 times" in {
      val xs = Source.fromIterator(() => List("a", "b", "c").iterator)
        .via(RepeatNTimes(3))
        .runWith(Sink.seq).futureValue
      
      xs should === (List("a", "a", "a", "b", "b", "b", "c", "c", "c"))
    }

    "show TestSink" in {
      val probe: Probe[String] = 
        Source.fromIterator(() => List("a").iterator)
        .via(RepeatNTimes(3))
          .runWith(TestSink.probe)

      probe.requestNext("a")
      probe.requestNext("a")
      probe.requestNext("a")
      probe.expectComplete()
    }
  }

}
