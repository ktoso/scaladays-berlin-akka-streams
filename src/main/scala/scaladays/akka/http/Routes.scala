package scaladays.akka.http

import akka.http.scaladsl.server.Route

trait Routes 
  extends Step3IncomingStreamRoutes 
with Step3WebsocketRoutes {

  def routes: Route =
    step3Routes ~ step3WsRoutes
}
