import sbt._
import Keys._

object BananaUtils extends Build {
  lazy val root: Project = Project(
    id = "banana-utils",
    base = file("."),
    settings = commonSettings
  ).aggregate(io, ioJena, sesameJena, prefixes)

  lazy val io: Project = Project(
    id = "banana-io",
    base = file("io"),
    settings = commonSettings
  )

  lazy val jena: Project = Project(
    id = "banana-jena",
    base = file("jena"),
    dependencies = Seq(io),
    settings = commonSettings
  )

  lazy val ioJena: Project = Project(
    id = "banana-io-jena",
    base = file("io-jena"),
    dependencies = Seq(io),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.github.jsonld-java" % "jsonld-java-jena" % "0.2" excludeAll(
          ExclusionRule(organization = "org.apache.jena"),
          ExclusionRule(organization = "org.slf4j")
        )
      )
    )
  )

  lazy val sesameJena: Project = Project(
    id = "banana-io-sesame",
    base = file("io-sesame"),
    dependencies = Seq(io),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.openrdf.sesame" % "sesame-rio-rdfjson" % "2.7.7"
      )
    )
  )

  lazy val prefixes: Project = Project(
    id = "banana-prefixes",
    base = file("prefixes"),
    settings = commonSettings ++ Seq(
      libraryDependencies <+= scalaVersion(
        "org.scala-lang" % "scala-compiler" % _
      )
    )
  )

  lazy val argonaut: Project = Project(
    id = "banana-argonaut",
    base = file("argonaut"),
    dependencies = Seq(io),
    settings = commonSettings ++ Seq(
      libraryDependencies += "io.argonaut" %% "argonaut" % "6.0"
    )
  )

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith",
    version := "0.0.0-SNAPSHOT",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalaVersion := "2.10.3",
    scalaBinaryVersion := "2.10",
    scalacOptions := Seq(
      "-feature",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.6",
      "org.typelevel" %% "scalaz-contrib-210" % "0.1.5"
    )
  )
}

