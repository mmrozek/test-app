package com.contactis.calculator.engine
import cats.Id
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import com.contactis.calculator
import com.contactis.calculator._
import cats.implicits._
import cats.instances._

//For test
object SimpleEvaluator extends Evaluator[Id] {
  override def evaluate(ast: calculator.Expr): ValidatedNel[EvaluationError, Double] = ast match {
    case Const(x) => Valid(x)
    case Add(x, y) => (evaluate(x), evaluate(y)).mapN(_ + _)
    case Sub(x, y) => (evaluate(x), evaluate(y)).mapN(_ - _)
    case Mult(x, y) => (evaluate(x), evaluate(y)).mapN(_ * _)
    case Div(x, y) => (evaluate(x), evaluate(y)).mapN(_ / _)
  }
}
