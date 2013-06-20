package edu.umd.mith.banana.io.jena

import com.github.jsonldjava.core.JSONLD
import com.github.jsonldjava.utils.JSONUtils
import com.github.jsonldjava.impl.JenaRDFParser
import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.umd.mith.banana.io.{ JsonLD, JsonLDContext }
import java.io.{ Writer => jWriter }
import org.w3.banana._
import org.w3.banana.jena.Jena
import scala.util._
import scalax.io._

abstract class JsonLDWriter[C: JsonLDContext]
  extends RDFWriter[Jena, JsonLD] {
  val syntax = JsonLD

  def context: C

  def contextMap = implicitly[JsonLDContext[C]].toMap(context)

  def write[R <: jWriter](
    graph: Jena#Graph,
    wcr: WriteCharsResource[R],
    base: String
  ): Try[Unit] = Try {
    wcr.acquireAndGet { writer =>
      val model = ModelFactory.createModelForGraph(graph.jenaGraph)
      val parser = new JenaRDFParser()
      val jsonld = JSONLD.compact(JSONLD.fromRDF(model, parser), contextMap)
      JSONUtils.writePrettyPrint(writer, jsonld)
    }
  }
}

