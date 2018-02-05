package com.contactis.calculator.parser
import cats.syntax.either._
import com.contactis.calculator._

object ExpressionParser extends scala.util.parsing.combinator.RegexParsers {

  def parse(input: String): Either[ParsingError, Expr] = {
    parseAll(expr, input) match {
      case Success(result, _) =>
        result.asRight[ParsingError]
      case NoSuccess(msg, _) =>
        ParsingError(msg).asLeft
    }
  }

  private def expr: Parser[Expr] = term ~ rep(plus | minus) ^^ { case a ~ b => b.foldLeft(a)((expr, op) => op(expr)) }

  private def plus: Parser[Expr => Expr] = "+" ~> term ^^ (b => a => Add(a, b))
  private def minus: Parser[Expr => Expr] = "-" ~> term ^^ (b => a => Sub(a, b))

  private def term: Parser[Expr] = factor ~ rep(mult | div) ^^ { case a ~ b => b.foldLeft(a)((expr, op) => op(expr)) }

  private def mult: Parser[Expr => Expr] = "*" ~> factor ^^ (b => a => Mult(a, b))
  private def div: Parser[Expr => Expr] = "/" ~> factor ^^ (b => a => Div(a, b))

  private def factor: Parser[Expr] =
    "(" ~> expr <~ ")" | "\\d+".r ^^ { x => Const(x.toInt) } | err("Expected a value")
}
