package scaladays.akka.stream

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

final case class RepeatNTimes[T](n: Int) extends GraphStage[FlowShape[T, T]] {
  val in = Inlet[T]("RepeatNTimes.in")
  val out = Outlet[T]("RepeatNTimes.out")

  override def shape: FlowShape[T, T] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {

    var element: T = null.asInstanceOf[T]
    var remainToBePushed: Int = 0
    var terminating: Boolean = false
    
    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        element = grab(in)
        if (remainToBePushed > 0) pushElement()
      }

      override def onUpstreamFinish(): Unit = {
        terminating = true
        if (remainToBePushed == 0) completeStage()
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        if (element != null) {
          pushElement()
          completeOrReset()
        } else {
          pull(in)
          remainToBePushed = n
        }
      }
    })

    private def completeOrReset(): Unit = {
      if (remainToBePushed == 0)
        if (terminating) completeStage()
        else element = null.asInstanceOf[T]
    }

    private def pushElement(): Unit = {
      push(out, element)
      remainToBePushed -= 1
    }
  }

}
