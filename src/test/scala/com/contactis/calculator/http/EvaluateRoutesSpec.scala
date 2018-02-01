package com.contactis.calculator.http

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class EvaluateRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest
    with EvaluateRoutes {

  lazy val routes = evaluateRoutes

  "EvaluateRoutes" should {
    "???" in { //todo
      val request = HttpRequest(uri = "/evaluate", method = HttpMethods.POST)

      request ~> routes ~> check {
        1 should ===(1)

      }
    }
  }
}
