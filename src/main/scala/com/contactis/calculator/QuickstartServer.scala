package com.contactis.calculator

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.contactis.calculator.evaluator.parallel.ActorBasedEvaluator
import com.contactis.calculator.http.EvaluateRoutes
import com.contactis.calculator.parser.ExpressionParser

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import cats.implicits._

object QuickstartServer extends App with EvaluateRoutes {

  implicit val system: ActorSystem = ActorSystem("CalculatorActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  override val evaluationService: (String) => Future[Either[ServiceError, Double]] = new EvaluationService[Future](
    ExpressionParser.parse, new ActorBasedEvaluator(system).evaluate
  ).evaluate

  lazy val routes: Route = evaluateRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
