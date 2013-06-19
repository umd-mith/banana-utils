import sbt._
import Keys._

object BananaUtils extends Build {
  lazy val root: Project = Project(
    id = "banana-utils",
    base = file("."),
    settings = commonSettings
  ).aggregate(prefixer)

  lazy val prefixer: Project = Project(
    id = "banana-utils-prefixes",
    base = file("prefixes"),
    settings = commonSettings ++ Seq(
      libraryDependencies <+= scalaVersion(
        "org.scala-lang" % "scala-compiler" % _
      )
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git"), "banana-rdf"),
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git"), "banana-sesame")
  )

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith",
    version := "0.0.0-SNAPSHOT",
    resolvers += "Sonatype snapshots" at
      "http://oss.sonatype.org/content/repositories/snapshots",
    scalaVersion := "2.10.2",
    scalaBinaryVersion := "2.10",
    scalacOptions := Seq(
      "-feature",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.6.4",
      "org.typelevel" %% "scalaz-contrib-210" % "0.1.4"
    )
  )
}

