package edu.umd.mith.banana.jena

import org.w3.banana.RDFOps
import org.w3.banana.jena.{ Jena => BananaJena, JenaModule => BananaJenaModule }

object Jena extends JenaModule

trait JenaModule extends BananaJenaModule {
  implicit override val ops: RDFOps[BananaJena] = JenaOperations
}

