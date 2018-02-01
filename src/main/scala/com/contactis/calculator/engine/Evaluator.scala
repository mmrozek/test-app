package com.contactis.calculator.engine

import cats.Monad
import cats.data.ValidatedNel
import com.contactis.calculator.Expr

abstract class Evaluator[M[_]: Monad] {
  def evaluate(ast: Expr): M[ValidatedNel[EvaluationError, Double]]
}
