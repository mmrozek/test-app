package com.contactis.calculator.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import com.contactis.calculator.evaluator.parallel.ActorBasedEvaluator
import com.contactis.calculator.parser.ExpressionParser
import com.contactis.calculator.{ EvaluationService, ServiceError }
import org.scalatest.{ Matchers, WordSpec }
import cats.implicits._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.duration._

class EvaluateRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with EvaluateRoutes with JsonSupport with ScalaFutures {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5 seconds)

  override val evaluationService: (String) => Future[Either[ServiceError, Double]] =
    new EvaluationService[Future](
      ExpressionParser.parse, new ActorBasedEvaluator(system).evaluate
    ).evaluate

  "EvaluateRoutes" should {

    "Respond with 200 and correct response" in {
      val entity = Marshal(Request("2+2")).to[MessageEntity].futureValue

      val request = Post("/evaluate").withEntity(entity)
      request ~> evaluateRoutes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[Response] shouldEqual Response(4.0)
      }
    }

    "Respond with Bad Request after parsing error" in {
      val entity = Marshal(Request("2+")).to[MessageEntity].futureValue

      val request = Post("/evaluate").withEntity(entity)
      request ~> evaluateRoutes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[String] shouldEqual "Expected a value"
      }
    }

    "Respond with Bad Request after evaluation error" in {
      val entity = Marshal(Request("8/0")).to[MessageEntity].futureValue

      val request = Post("/evaluate").withEntity(entity)
      request ~> evaluateRoutes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[String] shouldEqual "Dividing by 0"
      }
    }

  }
}
