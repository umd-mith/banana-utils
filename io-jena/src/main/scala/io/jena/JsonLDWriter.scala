package edu.umd.mith.banana.io.jena

import com.github.jsonldjava.core.JSONLD
import com.github.jsonldjava.utils.JSONUtils
import com.github.jsonldjava.impl.JenaRDFParser
import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.umd.mith.banana.io.{ JsonLD, JsonLDContext }
import java.io.{ OutputStream, OutputStreamWriter }
import org.w3.banana._
import org.w3.banana.jena.Jena
import scala.util._

abstract class JsonLDWriter[C: JsonLDContext]
  extends RDFWriter[Jena, JsonLD] {
  val syntax = JsonLD

  def context: C

  def contextMap = implicitly[JsonLDContext[C]].toMap(context)

  def write(
    graph: Jena#Graph,
    stream: OutputStream,
    base: String
  ): Try[Unit] = Try {
    val model = ModelFactory.createModelForGraph(graph)
    val parser = new JenaRDFParser()
    val jsonld = JSONLD.compact(JSONLD.fromRDF(model, parser), contextMap)
    val writer = new OutputStreamWriter(stream) 
    JSONUtils.writePrettyPrint(writer, jsonld)
  }
}

