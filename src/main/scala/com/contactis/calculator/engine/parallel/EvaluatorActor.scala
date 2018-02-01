package com.contactis.calculator.engine.parallel

import akka.pattern.{ ask, pipe }
import akka.actor.{ Actor, ActorContext, PoisonPill, Props }
import akka.util.Timeout
import cats.data.Validated.Valid

import scala.concurrent.duration._
import cats.data.ValidatedNel
import com.contactis.calculator._
import com.contactis.calculator.engine.EvaluationError
import com.contactis.calculator.engine.parallel.EvaluatorActor.protocol.Eval
import cats.implicits._
import com.contactis.calculator.engine.parallel.EvaluatorActor.EvaluatorActorResponse

import scala.concurrent.ExecutionContext.Implicits.global //todo

class EvaluatorActor extends Actor {

  implicit val timeout = Timeout(5 seconds)

  override def receive: Receive = {
    case Eval(ast) => ast match {
      case Const(x) =>
        sender() ! Valid(x)
        self ! PoisonPill
      case Add(x, y) => eval(x, y, _ + _)(context)
      case Sub(x, y) => eval(x, y, _ - _)(context)
      case Mult(x, y) => eval(x, y, _ * _)(context)
      case Div(x, y) => eval(x, y, _ / _)(context)
    }
  }

  private def eval(x: Expr, y: Expr, f: (Double, Double) => Double)(ctx: ActorContext) = {
    val worker1 = ctx.actorOf(EvaluatorActor.props())
    val worker2 = ctx.actorOf(EvaluatorActor.props())

    val resp1 = (worker1 ? Eval(x)).mapTo[EvaluatorActorResponse[Double]]
    val resp2 = (worker2 ? Eval(y)).mapTo[EvaluatorActorResponse[Double]]

    pipe((resp1, resp2).mapN((a, b) => (a, b).mapN(f))) to sender()
    self ! PoisonPill
  }
}

object EvaluatorActor {
  def props() = Props(new EvaluatorActor)

  type EvaluatorActorResponse[T] = ValidatedNel[EvaluationError, T]

  object protocol {
    case class Eval(expr: Expr)
  }
}
