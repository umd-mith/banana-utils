package edu.umd.mith.banana.prefixes.utils

trait ReflectionUtils {
  def constructor(u: scala.reflect.api.Universe) = {
    import u._
 
    DefDef(
      Modifiers(),
      nme.CONSTRUCTOR,
      Nil,
      Nil :: Nil,
      TypeTree(),
      Block(
        Apply(
          Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR),
          Nil
        ) :: Nil,
        Literal(Constant(()))
      )
    )
  }

  def isDefined(u: scala.reflect.api.Universe)(sym: u.MethodSymbol) =
    !sym.asInstanceOf[scala.reflect.internal.Symbols#Symbol].hasFlag(
      scala.reflect.internal.Flags.DEFERRED
    )
}

trait MacroUtils extends ReflectionUtils {
  import scala.reflect.macros.Context

  def thisValName(c: Context): Option[String] = {
    import c.universe._

    c.enclosingClass match {
      case ClassDef(_, _, _, Template(_, _, body)) => body.collectFirst {
        case ValDef(_, name, _, tree) if tree.pos == c.enclosingPosition =>
          name.decoded 
      }
    }
  }
}

