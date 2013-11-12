package edu.umd.mith.banana.io.sesame

import edu.umd.mith.banana.io.RDFJson
import java.io.{ OutputStream, OutputStreamWriter }
import org.openrdf.rio.rdfjson.RDFJSONWriterFactory
import org.w3.banana._
import org.w3.banana.sesame.{ Sesame, SesameOperations }
import scala.util._

trait RDFJsonWriter extends RDFWriter[Sesame, RDFJson] {
  val syntax = RDFJson

  def write(
    graph: Sesame#Graph,
    stream: OutputStream,
    base: String
  ): Try[Unit] = Try {
    val factory = new RDFJSONWriterFactory
    val writer = factory.getWriter(stream)
    writer.startRDF()
    SesameOperations.graphToIterable(graph) foreach writer.handleStatement
    writer.endRDF()
  }
}

