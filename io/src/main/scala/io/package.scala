package edu.umd.mith.banana

import org.w3.banana.{ MimeType, Syntax }
import scalaz.NonEmptyList

package object io {
  implicit val JsonLD = new Syntax[JsonLD] {
    val mimeTypes = NonEmptyList(MimeType("application/ld+json"))
  }

  implicit val RDFJson = new Syntax[RDFJson] {
    val mimeTypes = NonEmptyList(MimeType("application/rdf+json"))
  }

  implicit object NamespaceMapJsonLDContext
    extends JsonLDContext[Map[String, String]] {
    def toMap(ctx: Map[String, String]) = {
      val javaCtx = new java.util.HashMap[String, Object]()
       ctx.foreach { case (k, v) => javaCtx.put(k, v) }
       javaCtx
    }
  }
}

