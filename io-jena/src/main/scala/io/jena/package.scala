package edu.umd.mith.banana.io

package object jena {
  implicit def JsonLDWriter[C: JsonLDContext](
    implicit ctx: C
  ) = new JsonLDWriter[C] {
    val context = ctx
  }

  implicit def RDFJsonWriter = new RDFJsonWriter {}
}

