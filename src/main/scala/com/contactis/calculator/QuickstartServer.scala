package com.contactis.calculator

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.contactis.calculator.http.EvaluateRoutes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuickstartServer extends App with EvaluateRoutes {

  implicit val system: ActorSystem = ActorSystem("CalculatorActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val routes: Route = evaluateRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
