package edu.umd.mith.banana.io.jena

import com.github.jsonldjava.core.JSONLD
import com.github.jsonldjava.utils.JSONUtils
import com.github.jsonldjava.impl.JenaRDFParser
import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.umd.mith.banana.io.RDFJson
import java.io.{ OutputStream, OutputStreamWriter }
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.Lang
import org.w3.banana._
import org.w3.banana.jena.Jena
import scala.util._

trait RDFJsonWriter extends RDFWriter[Jena, RDFJson] {
  val syntax = RDFJson

  def write(
    graph: Jena#Graph,
    stream: OutputStream,
    base: String
  ): Try[Unit] = Try {
    val model = ModelFactory.createModelForGraph(graph)
    RDFDataMgr.write(stream, model, Lang.RDFJSON)
  }
}

