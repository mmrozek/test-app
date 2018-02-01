package com.contactis.calculator.engine.parallel

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import cats.data.ValidatedNel

import scala.concurrent.duration._
import com.contactis.calculator
import com.contactis.calculator.engine.{ EvaluationError, Evaluator }
import cats.implicits._
import com.contactis.calculator.engine.parallel.EvaluatorActor.EvaluatorActorResponse
import com.contactis.calculator.engine.parallel.EvaluatorActor.protocol.Eval

import scala.concurrent.{ ExecutionContext, Future }

class ActorBasedEvaluator(system: ActorSystem)(implicit ec: ExecutionContext) extends Evaluator[Future] {

  implicit val timeout = Timeout(5 seconds)

  override def evaluate(ast: calculator.Expr): Future[ValidatedNel[EvaluationError, Double]] = {
    val root = system.actorOf(EvaluatorActor.props())
    (root ? Eval(ast)).mapTo[EvaluatorActorResponse[Double]]
  }

}

