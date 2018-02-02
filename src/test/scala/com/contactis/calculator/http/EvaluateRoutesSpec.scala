package com.contactis.calculator.http

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.contactis.calculator.evaluator.parallel.ActorBasedEvaluator
import com.contactis.calculator.parser.ExpressionParser
import com.contactis.calculator.{EvaluationService, ServiceError}
import org.scalatest.{Matchers, WordSpec}
import cats.implicits._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EvaluateRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with EvaluateRoutes with JsonSupport with ScalaFutures {

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
  }
}
