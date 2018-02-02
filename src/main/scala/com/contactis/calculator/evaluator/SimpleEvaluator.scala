package com.contactis.calculator.evaluator
import cats.Id
import cats.data.Validated.{ Invalid, Valid }
import cats.data.{ NonEmptyList, ValidatedNel }
import com.contactis.calculator
import com.contactis.calculator._
import cats.implicits._

//For test
object SimpleEvaluator extends Evaluator[Id] {
  override def evaluate(ast: calculator.Expr): ValidatedNel[EvaluationError, Double] = ast match {
    case Const(x) => Valid(x)
    case Add(x, y) => (evaluate(x), evaluate(y)).mapN(_ + _)
    case Sub(x, y) => (evaluate(x), evaluate(y)).mapN(_ - _)
    case Mult(x, y) => (evaluate(x), evaluate(y)).mapN(_ * _)
    case Div(x, y) =>
      val divisor = evaluate(y)
      if (divisor != Valid(0))
        (evaluate(x), divisor).mapN(_ / _)
      else
        Invalid(NonEmptyList(DividingByZeroError(), Nil))
  }
}
