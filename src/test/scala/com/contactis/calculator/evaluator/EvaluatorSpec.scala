package com.contactis.calculator.evaluator

import akka.actor.ActorSystem
import akka.testkit.TestKit
import cats.Id
import cats.data.Validated.{ Invalid, Valid }
import cats.data.{ NonEmptyList, ValidatedNel }
import com.contactis.calculator._
import com.contactis.calculator.evaluator.parallel.ActorBasedEvaluator
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class EvaluatorSpec extends TestKit(ActorSystem("EvaluatorSpec")) with WordSpecLike with Matchers
    with EvaluatorBehaviour with BeforeAndAfterAll with ScalaFutures {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "SimpleEvaluator" should {
    behave like evaluator(SimpleEvaluator)
  }

  "ActorBasedEvaluator" should {

    def synchronousFutureEvaluator(e: Evaluator[Future]): Evaluator[Id] = {
      new Evaluator[Id] {
        val timeout = Timeout(5 seconds)
        override def evaluate(ast: Expr): ValidatedNel[EvaluationError, Double] = e.evaluate(ast).futureValue(timeout)
      }
    }

    behave like evaluator(synchronousFutureEvaluator(new ActorBasedEvaluator(system)))
  }

}

trait EvaluatorBehaviour {
  self: WordSpecLike with Matchers =>

  def evaluator[M[_]](e: => Evaluator[M]) = {

    "return a result from a const" in {
      e.evaluate(Const(1.2)) should ===(Valid(1.2))
    }

    "return a result from a simple AST" in {
      e.evaluate(Add(Const(1), Const(2))) should ===(Valid(3.0))
    }

    "return a result from a complex AST" in {
      e.evaluate(Sub(Const(3), Mult(Add(Const(2), Const(2)), Const(6)))) should ===(Valid(-21.0))
    }

    "return an error when dividing by 0" in {
      e.evaluate(Div(Const(2), Const(0))) should ===(Invalid(NonEmptyList(DividingByZeroError(), Nil)))
    }

    "concatenate evaluation errors" in {
      e.evaluate(Mult(Div(Const(2), Const(0)), Div(Const(2), Const(0)))) should
        ===(Invalid(NonEmptyList(DividingByZeroError(), DividingByZeroError() :: Nil)))
    }
  }

}
