package edu.umd.mith.banana.io.jena

import com.github.jsonldjava.core.JSONLD
import com.github.jsonldjava.utils.JSONUtils
import com.github.jsonldjava.impl.JenaRDFParser
import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.umd.mith.banana.io.RDFJson
import java.io.{ Writer => jWriter }
import org.openjena.riot.system.JenaWriterRdfJson
import org.w3.banana._
import org.w3.banana.jena.Jena
import scala.util._
import scalax.io._

trait RDFJsonWriter extends RDFWriter[Jena, RDFJson] {
  val syntax = RDFJson

  def write[R <: jWriter](
    graph: Jena#Graph,
    wcr: WriteCharsResource[R],
    base: String
  ): Try[Unit] = Try {
    wcr.acquireAndGet { writer =>
      val model = ModelFactory.createModelForGraph(graph.jenaGraph)
      new JenaWriterRdfJson().write(model, writer, null)
    }
  }
}

