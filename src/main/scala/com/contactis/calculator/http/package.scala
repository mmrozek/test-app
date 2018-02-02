package com.contactis.calculator

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

package object http {

  case class Request(expression: String)
  case class Response(result: Double)

  trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val requestFormat = jsonFormat1(Request)
    implicit val responseFormat = jsonFormat1(Response)
  }

}
