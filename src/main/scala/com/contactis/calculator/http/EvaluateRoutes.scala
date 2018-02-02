package com.contactis.calculator.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.model.StatusCodes._
import com.contactis.calculator.ServiceError

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait EvaluateRoutes extends JsonSupport {
  implicit def system: ActorSystem

  val evaluationService: String => Future[Either[ServiceError, Double]]

  lazy val log = Logging(system, classOf[EvaluateRoutes])

  lazy val evaluateRoutes: Route =
    pathPrefix("evaluate") {
      pathEnd {
        post {
          entity(as[Request]){
            request: Request => onComplete(evaluationService(request.expression)){
              case Success(Right(value)) => complete(Response(value))
              case Success(Left(error)) => complete((BadRequest, error.details))
              case Failure(err) => complete((InternalServerError, err.getMessage))
            }
          }
        }
      }
    }
}
