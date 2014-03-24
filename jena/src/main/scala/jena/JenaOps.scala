package edu.umd.mith.banana.jena

import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.graph.Factory
import org.w3.banana.jena.{
  Jena => BananaJena,
  JenaOperations => BananaJenaOperations
}

object JenaOperations extends BananaJenaOperations {
  override val emptyGraph = Factory.createDefaultGraph

  override def makeGraph(triples: Iterable[BananaJena#Triple]) = {
    val graph = Factory.createDefaultGraph
    triples.foreach(graph.add)
    graph
  }

  override def union(graphs: Seq[BananaJena#Graph]) = {
    val g = Factory.createDefaultGraph
    graphs.foreach { graph =>
      val it = graph.find(Node.ANY, Node.ANY, Node.ANY)
      while (it.hasNext) { g.add(it.next()) }
    }
    g
  }
}

