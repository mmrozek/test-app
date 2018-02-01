package com.contactis

package object calculator {

  trait Expr

  case class Add(x: Expr, y: Expr) extends Expr
  case class Sub(x: Expr, y: Expr) extends Expr
  case class Mult(x: Expr, y: Expr) extends Expr
  case class Div(x: Expr, y: Expr) extends Expr
  case class Const(x: Double) extends Expr

}
