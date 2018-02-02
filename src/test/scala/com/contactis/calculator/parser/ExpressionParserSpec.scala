package com.contactis.calculator.parser

import org.scalatest.{ Matchers, WordSpec }
import com.contactis.calculator._

class ExpressionParserSpec extends WordSpec with Matchers {

  import ExpressionParser._

  "ExpressionParser" should {

    "return correct AST for single const" in {
      val input = "2"
      parse(input) should ===(Right(Const(2)))
    }

    "return correct AST for simple expression" in {
      val input = "2+2"
      parse(input) should ===(Right(Add(Const(2), Const(2))))
    }

    "return correct AST for complex expression" in {
      val input = "3-(2+2)*6"
      parse(input) should ===(Right(Sub(Const(3), Mult(Add(Const(2), Const(2)), Const(6)))))
    }

    "return a parsing error for broken expression" in {
      val input = "3-"
      parse(input) should ===(Left(ParsingError("Expected a value")))
    }
  }

}
