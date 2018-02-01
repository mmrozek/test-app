package com.contactis.calculator.parser
import cats.syntax.either._
import com.contactis.calculator._

object ExpressionParser extends scala.util.parsing.combinator.RegexParsers {

  def parse(input: String) : Either[ParsingError, Expr] = {
    parseAll(expr, input) match {
      case Success(result, _) =>
        result.asRight[ParsingError]
      case NoSuccess(msg, _) =>
        ParsingError(msg).asLeft
    }
  }

  private def expr : Parser[Expr] =
    (term<~"+")~expr ^^ { case l~r => Add(l, r) } | (term<~"-")~expr ^^ { case l~r => Sub(l, r) } | term

  private def term : Parser[Expr] =
    (factor<~"*")~term ^^ { case l~r => Mult(l, r) } | (factor<~"/")~term ^^ { case l~r => Div(l, r) } | factor

  private def factor : Parser[Expr] =
    "("~>expr<~")" | "\\d+".r ^^ { x => Const(x.toInt) } | err("Expected a value")
}
