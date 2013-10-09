package edu.umd.mith.banana

import argonaut._, Argonaut._
import edu.umd.mith.banana.io.JsonLDContext
import scala.collection.JavaConverters._

package object argo {
  implicit object ArgoJsonLDContext extends JsonLDContext[Json] {
    def toObj(j: Json): Object = j.fold(
      null,
      boolean2Boolean,
      double2Double,
      identity,
      _.map(toObj).asJava,
      _.toMap.mapValues(toObj).asJava
    )

    def toMap(ctx: Json): java.util.Map[String, Object] =
      ctx.objectOrEmpty.toMap.mapValues(toObj).asJava
  }
}

