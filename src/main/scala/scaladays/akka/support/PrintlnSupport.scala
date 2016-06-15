package scaladays.akka.support

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.FiniteDuration

object PrintlnSupport extends PrintlnSupport

trait PrintlnSupport {
  
  def printlnSink[T](name: String) =
    Sink.foreach[T](it => println(s"$name: $it"))
  
  // specialized for our demo
  def printlnEvery[T](interval: FiniteDuration): Sink[T, NotUsed] = {
    val ticks = Source.tick(interval, interval, ())

    Flow[T]
      .conflateWithSeed[(Int, Option[T])](_ => 0 -> None) { case (aggregated, n) => (aggregated._1 + 1, Some(n)) }
      .zipWith(ticks) { (counter, ticks) => counter } // show Keep.left
      .to(Sink.foreach { case (n, last) => println(s"[$n] elements passed through during [$interval], last: [$last]...") })
      .withAttributes(Attributes.inputBuffer(1, 1))
  }

}
