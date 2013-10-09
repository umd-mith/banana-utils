package edu.umd.mith.banana.prefixes.utils

import java.io.InputStream
import org.w3.banana._
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import scala.util.Try
  scalaz._,
  scalaz.std.list._,
  scalaz.syntax.apply._,
  scalaz.syntax.traverse._
import scalaz.contrib.std.utilTry._

class SchemaParser[Rdf <: RDF](implicit
  val ops: RDFOps[Rdf],
  val sparqlOps: SparqlOps[Rdf],
  val reader: RDFReader[Rdf, RDFXML],
  val sparqlGraph: SparqlGraph[Rdf] 
) {
  import ops._
  import sparqlOps._

  def parse(resource: InputStream)(
    name: String,
    iri: String
  ): Try[(List[String], List[String])] =
    reader.read(resource, iri).flatMap { graph =>
      val base = URI(iri)
      val engine = sparqlGraph(graph)

      def toName(uri: Rdf#URI) = {
        val rel = base.relativize(uri).getString
        if (rel.charAt(0) == '#') rel.substring(1) else rel
      }

      val properties = engine.executeSelect(SelectQuery("""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        SELECT DISTINCT ?uri WHERE {
          ?uri a rdf:Property
        }
      """)).toIterable.toList.traverseU {
        _("uri").flatMap(_.as[Rdf#URI].map(toName))
      }

      val classes = engine.executeSelect(SelectQuery("""
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        SELECT DISTINCT ?uri WHERE {
          ?uri a rdfs:Class
        }
      """)).toIterable.toList.traverseU {
        _("uri").flatMap(_.as[Rdf#URI].map(toName))
      }

      properties tuple classes
    }
}

