package com.contactis.calculator

import cats.Id
import cats.data.NonEmptyList
import cats.data.Validated.{ Invalid, Valid }
import com.contactis.calculator.evaluator.DividingByZeroError
import com.contactis.calculator.parser.ParsingError
import org.scalatest.{ Matchers, WordSpec }

class EvaluationServiceSpec extends WordSpec with Matchers {
  "EvaluationService" should {
    "return a result when parsing and evaluating are correct" in {
      val service = new EvaluationService[Id](_ => Right(Const(1.0)), _ => Valid(1.0))
      service.evaluate("1.0") should ===(Right(1.0))
    }

    "return a parsing error when parsing is not correct" in {
      val service = new EvaluationService[Id](_ => Left(ParsingError("error")), _ => Valid(1.0))
      service.evaluate("1.0") should ===(Left(ParsingServiceError("error")))
    }

    "return an evaluation error when evaluating is not correct" in {
      val service = new EvaluationService[Id](_ => Right(Const(1.0)), _ => Invalid(NonEmptyList(DividingByZeroError(), Nil)))
      service.evaluate("1.0") should ===(Left(EvaluationServiceError("Dividing by 0")))
    }

    "concatenate evaluation errors" in {
      val service = new EvaluationService[Id](
        _ => Right(Const(1.0)), _ => Invalid(NonEmptyList(DividingByZeroError(), DividingByZeroError() :: Nil))
      )
      service.evaluate("1.0") should ===(Left(EvaluationServiceError("Dividing by 0, Dividing by 0")))
    }
  }
}
