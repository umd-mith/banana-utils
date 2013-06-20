package edu.umd.mith.banana.io

trait JsonLD
trait RDFJson

trait JsonLDContext[C] {
  def toMap(ctx: C): java.util.Map[String, Object]
}

