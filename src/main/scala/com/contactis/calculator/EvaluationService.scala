package com.contactis.calculator

import cats.Monad
import cats.data.{EitherT, ValidatedNel}
import cats.implicits._
import com.contactis.calculator.evaluator.EvaluationError
import com.contactis.calculator.parser.ParsingError

class EvaluationService[M[_]: Monad](
                                      parser: String => Either[ParsingError, Expr],
                                      evaluator: Expr => M[ValidatedNel[EvaluationError, Double]]
                             ) {

  def evaluate(input: String): M[Either[ServiceError, Double]]  = (for {
    ast <- EitherT(Monad[M].pure(parser(input).left.map(e => ParsingServiceError(e.details))))
    result <- EitherT[M,ServiceError, Double](evaluator(ast).map(x => x.leftMap(err => EvaluationServiceError(err.map(_.details).toList.mkString(", "))).toEither))
  } yield result).value

}

sealed trait ServiceError

case class ParsingServiceError(details: String) extends ServiceError
case class EvaluationServiceError(details: String) extends ServiceError
