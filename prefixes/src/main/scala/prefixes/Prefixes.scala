package edu.umd.mith.banana.prefixes

import edu.umd.mith.banana.prefixes.utils._
import java.io.InputStream
import org.w3.banana._
import org.w3.banana.sesame._
import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.macros.Context
import scala.util._
import scalax.io.Resource

trait Prefixes[Rdf <: RDF] {
  def ops: RDFOps[Rdf]

  def create[P[R <: RDF] <: Prefix[R]]: P[Rdf] =
    macro PrefixMacros.create_impl[Rdf, P]

  def createWithSchema[P[R <: RDF] <: Prefix[R]](path: String): P[Rdf] =
    macro PrefixMacros.createWithSchema_impl[Rdf, P]

  def createFromSchema[Rdf <: RDF](iri: String, path: String) =
    macro PrefixMacros.createFromSchema_impl[Rdf]
}

private object PrefixMacros extends MacroUtils {
  lazy val parser = new SchemaParser[Sesame]

  def create_impl[
    Rdf <: RDF,
    P[R <: RDF] <: Prefix[R]
  ](c: Context)(implicit
    rdfTag: c.WeakTypeTag[Rdf],
    prefixTag: c.WeakTypeTag[P[_]]
  ): c.Expr[P[Rdf]] = this.create[Rdf, P](None)(c)(rdfTag, prefixTag)

  def createWithSchema_impl[
    Rdf <: RDF,
    P[R <: RDF] <: Prefix[R]
  ](c: Context)(path: c.Expr[String])(implicit
    rdfTag: c.WeakTypeTag[Rdf],
    prefixTag: c.WeakTypeTag[P[_]]
  ): c.Expr[P[Rdf]] = {
    import c.universe._

    val iri = c.enclosingUnit.body.find {
      case ClassDef(_, name, _, _) =>
        name == prefixTag.tpe.typeSymbol.name
      case _ => false
    }.map {
      case ClassDef(_, _, _, Template(_, _, body)) =>
        body.collectFirst {
          case ValDef(_, name, _, Literal(Constant(s: String))) 
            if name == newTermName("prefixIri") => s
        }.getOrElse(
          c.abort(
            c.enclosingPosition,
            "Prefix IRIs must be defined as string literals."
          )
        )
    }.getOrElse(
      c.abort(
        c.enclosingPosition,
        "Prefixes must be defined in the same compilation unit."
      )
    )

    val stream = path.tree match {
      case Literal(Constant(s: String)) =>
        Option(this.getClass.getResourceAsStream(s)).getOrElse(
          c.abort(c.enclosingPosition, "Invalid resource path!")
        )
      case _ => c.abort(
        c.enclosingPosition,
        "You must provide a literal resource path for schema parsing!"
      )
    }
 
    this.create[Rdf, P](Some(iri, stream))(c)(rdfTag, prefixTag)
  }

  def create[
    Rdf <: RDF,
    P[R <: RDF] <: Prefix[R]
  ](
    iriAndStream: Option[(String, InputStream)]
  )(
    c: Context
  )(implicit
    rdfTag: c.WeakTypeTag[Rdf],
    prefixTag: c.WeakTypeTag[P[_]]
  ): c.Expr[P[Rdf]] = { 
    import c.universe._

    val name = thisValName(c).getOrElse(
      c.abort(c.enclosingPosition, "Prefixes must be defined as values!")
    )

    val prefixType = appliedType(prefixTag.tpe, rdfTag.tpe :: Nil)
    val uriSymbol = rdfTag.tpe.member(newTypeName("URI")).asType

    val names = prefixType.declarations.collect {
      case m: MethodSymbol if
        m.returnType.typeSymbol == uriSymbol &&
        !isDefined(c.universe)(m) => m.name.decoded
    }

    iriAndStream.foreach {
      case (iri, stream) =>
        parser.parse(Resource.fromInputStream(stream))(name, iri) match {
          case Failure(error) =>
            c.abort(c.enclosingPosition, "Invalid schema: " + error)
          case Success((properties, classes)) =>
            names.foreach {
              case name if
                name.charAt(0).isLower && !properties.contains(name) =>
                  c.abort(
                    c.enclosingPosition,
                    "The following is not a valid property name: " + name
                  )
              case name if
                name.charAt(0).isUpper && !classes.contains(name) =>
                  c.abort(
                    c.enclosingPosition,
                    "The following is not a valid class name: " + name
                  )
              case _ => ()
            }
      }
    }

    val vals = names.map { name =>
      ValDef(
        Modifiers(),
        newTermName(name),
        TypeTree(),
        Apply(
          Select(This(tpnme.EMPTY), newTermName("apply")),
          c.literal(name).tree :: Nil
        )
      )
    }.toList

    val anon = newTypeName(c.fresh())

    c.Expr[P[Rdf]](
      Block(
        ClassDef(
          Modifiers(Flag.FINAL),
          anon,
          Nil,
          Template(
            TypeTree(
              appliedType(
                c.weakTypeOf[P[_]].typeConstructor,
                rdfTag.tpe :: Nil
              )
            ) :: Nil,
            emptyValDef,
            List(
              constructor(c.universe),
              DefDef(
                Modifiers(),
                newTermName("apply"),
                Nil,
                List(
                  ValDef(
                    Modifiers(Flag.PARAM),
                    newTermName("value"),
                    Ident(typeOf[String].typeSymbol),
                    EmptyTree
                  ) :: Nil
                ),
                TypeTree(),
                Apply(
                  Select(
                    Ident(newTermName("ops")),
                    newTermName("makeUri")
                  ),
                  Apply(
                    Select(
                      Select(
                        This(tpnme.EMPTY),
                        newTermName("prefixIri")
                      ),
                      newTermName("$plus")
                    ),
                    Ident(newTermName("value")) :: Nil
                  ) :: Nil
                )
              ),
              DefDef(
                Modifiers(),
                newTermName("unapply"),
                Nil,
                List(
                  ValDef(
                    Modifiers(Flag.PARAM),
                    newTermName("iri"),
                    SelectFromTypeTree(
                      Ident(newTypeName("Rdf")),
                      newTypeName("URI")
                    ),
                    EmptyTree
                  ) :: Nil
                ),
                TypeTree(),
                Block(
                  ValDef(
                    Modifiers(),
                    newTermName("uriString"),
                    TypeTree(),
                    Apply(
                      Select(
                        Ident(newTermName("ops")),
                        newTermName("fromUri")
                      ),
                      Ident(newTermName("iri")) :: Nil
                    )
                  ) :: Nil,
                  If(
                    Apply(
                      Select(
                        Ident(newTermName("uriString")),
                        newTermName("startsWith")
                      ),
                      Select(
                        This(tpnme.EMPTY),
                        newTermName("prefixIri")
                      ) :: Nil
                    ),
                    Apply(
                      Select(
                        Ident(typeOf[Some.type].typeSymbol),
                        newTermName("apply")
                      ),
                      Apply(
                        Select(
                          Ident(newTermName("uriString")),
                          newTermName("substring")
                        ),
                        Apply(
                          Select(
                            Select(
                              This(tpnme.EMPTY),
                              newTermName("prefixIri")
                            ),
                            newTermName("length")
                          ),
                          Nil
                        ) :: Nil
                      ) :: Nil
                    ),
                    reify(None).tree
                  )
                )    
              ),
              ValDef(
                Modifiers(),
                newTermName("prefixName"),
                TypeTree(),
                c.literal(name).tree
              )
            ) ::: vals
          )
        ) :: Nil,
        Typed(
          Apply(Select(New(Ident(anon)), nme.CONSTRUCTOR), Nil),
          TypeTree(
            appliedType(
              c.weakTypeOf[P[_]].typeConstructor,
              rdfTag.tpe :: Nil
            )
          )
        )
      )
    )
  }

  def createFromSchema_impl[Rdf <: RDF: c.WeakTypeTag](c: Context)(
    iri: c.Expr[String],
    path: c.Expr[String]
  ) = {
    import c.universe._
 
    val name = thisValName(c).getOrElse(
      c.abort(c.enclosingPosition, "Prefixes must be defined as values!")
    )

    val iriLit = iri.tree match {
      case Literal(Constant(s: String)) => s
      case _ => c.abort(
        c.enclosingPosition,
        "You must provide a literal URI!"
      )
    }

    val stream = path.tree match {
      case Literal(Constant(s: String)) =>
        Option(this.getClass.getResourceAsStream(s)).getOrElse(
          c.abort(c.enclosingPosition, "Invalid resource path!")
        )
      case _ => c.abort(
        c.enclosingPosition,
        "You must provide a literal resource path for schema parsing!"
      )
    }

    parser.parse(Resource.fromInputStream(stream))(name, iriLit) match {
      case Failure(error) =>
        c.abort(c.enclosingPosition, "Invalid schema: " + error)
      case Success((properties, classes)) =>
        val anon = newTypeName(c.fresh())
        val wrapper = newTypeName(c.fresh())
        val applier = Select(This(anon), newTermName("apply"))

        val defs = (properties ++ classes).map { name =>
          ValDef(
            Modifiers(),
            newTermName(name),
            TypeTree(),
            Apply(applier, c.literal(name).tree :: Nil)
          )
        }

        c.Expr[PrefixBuilder[Rdf]](
          Block(
            List(
              ClassDef(
                Modifiers(),
                anon,
                Nil,
                Template(
                  Ident(weakTypeOf[PrefixBuilder[Rdf]].typeSymbol) :: Nil,
                  emptyValDef,
                  DefDef(
                    Modifiers(),
                    nme.CONSTRUCTOR,
                    Nil,
                    Nil :: Nil,
                    TypeTree(),
                    Block(
                      Apply(
                        Apply(
                          Select(
                            Super(This(tpnme.EMPTY), tpnme.EMPTY),
                            nme.CONSTRUCTOR
                          ),
                          List(
                            c.literal(name).tree,
                            c.literal(iriLit).tree  
                          )
                        ),
                        Ident(newTermName("ops")) :: Nil
                      ) :: Nil,
                      c.literalUnit.tree
                    )
                  ) :: defs
                )
              ),
              ClassDef(
                Modifiers(Flag.FINAL),
                wrapper,
                Nil,
                Template(
                  Ident(anon) :: Nil,
                  emptyValDef,
                  constructor(c.universe) :: Nil
                )
              )
            ),
            Apply(Select(New(Ident(wrapper)), nme.CONSTRUCTOR), Nil)
          )
        )
    }
  }
}

