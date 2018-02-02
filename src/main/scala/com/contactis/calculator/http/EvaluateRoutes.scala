package com.contactis.calculator.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import com.contactis.calculator.EvaluationService

trait EvaluateRoutes {
  implicit def system: ActorSystem

//  val service: EvaluationService

  lazy val log = Logging(system, classOf[EvaluateRoutes])

  lazy val evaluateRoutes: Route =
    pathPrefix("evaluate") {
      pathEnd {
        post {
          ???
        }
      }
    }
}
