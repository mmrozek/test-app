package com.contactis.calculator.evaluator

trait EvaluationError {
  def details: String
}

case class DividingByZeroError(details: String = "Dividing by 0") extends EvaluationError
