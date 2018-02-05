package com.contactis.calculator.evaluator.parallel

import akka.pattern.{ ask, pipe }
import akka.actor.{ Actor, ActorContext, PoisonPill, Props }
import akka.util.Timeout
import cats.data.Validated.{ Invalid, Valid }

import scala.concurrent.duration._
import cats.data.{ NonEmptyList, ValidatedNel }
import com.contactis.calculator._
import com.contactis.calculator.evaluator.{ DividingByZeroError, EvaluationError }
import com.contactis.calculator.evaluator.parallel.EvaluatorActor.protocol.Eval
import cats.implicits._
import com.contactis.calculator.evaluator.parallel.EvaluatorActor.EvaluatorActorResponse

private[evaluator] class EvaluatorActor extends Actor {

  implicit val timeout = Timeout(5 seconds)

  implicit def ec = context.dispatcher

  override def receive: Receive = {
    case Eval(ast) => ast match {
      case Const(x) =>
        sender() ! Valid(x)
        self ! PoisonPill
      case Add(x, y) => eval(x, y, (x, y) => (x, y).mapN(_ + _))(context)
      case Sub(x, y) => eval(x, y, (x, y) => (x, y).mapN(_ - _))(context)
      case Mult(x, y) => eval(x, y, (x, y) => (x, y).mapN(_ * _))(context)
      case Div(x, y) => eval(x, y, (x, y) =>
        if (y != Valid(0)) (x, y).mapN(_ / _) else Invalid(NonEmptyList(DividingByZeroError(), Nil)))(context)
    }
  }

  private def eval(x: Expr, y: Expr, f: (EvaluatorActorResponse[Double], EvaluatorActorResponse[Double]) => ValidatedNel[EvaluationError, Double])(ctx: ActorContext) = {
    val worker1 = ctx.actorOf(EvaluatorActor.props())
    val worker2 = ctx.actorOf(EvaluatorActor.props())

    val resp1 = (worker1 ? Eval(x)).mapTo[EvaluatorActorResponse[Double]]
    val resp2 = (worker2 ? Eval(y)).mapTo[EvaluatorActorResponse[Double]]

    val result = (resp1, resp2).mapN((a, b) => f(a, b))

    pipe(result) to sender()

    result.onComplete(_ => self ! PoisonPill)
  }
}

private[evaluator] object EvaluatorActor {
  def props() = Props(new EvaluatorActor)

  type EvaluatorActorResponse[T] = ValidatedNel[EvaluationError, T]

  object protocol {
    case class Eval(expr: Expr)
  }
}
