import sbt._
import Keys._

object BananaUtils extends Build {
  lazy val root: Project = Project(
    id = "banana-utils",
    base = file("."),
    settings = commonSettings
  ).aggregate(io, ioJena, prefixes)

  lazy val io: Project = Project(
    id = "banana-io",
    base = file("io"),
    settings = commonSettings
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git#259d7e17a9c7aa72dec9abe8c0bb61ea9e49e3bd"), "banana-rdf")
  )

  lazy val ioJena: Project = Project(
    id = "banana-io-jena",
    base = file("io-jena"),
    dependencies = Seq(io),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        //"org.slf4j" % "slf4j-simple" % "1.6.4",
        "com.github.jsonld-java" % "jsonld-java-jena" % "0.1" excludeAll(
          ExclusionRule(organization = "org.apache.jena"),
          ExclusionRule(organization = "org.slf4j")
        )
      )
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git#259d7e17a9c7aa72dec9abe8c0bb61ea9e49e3bd"), "banana-jena")
  )

  lazy val prefixes: Project = Project(
    id = "banana-prefixes",
    base = file("prefixes"),
    settings = commonSettings ++ Seq(
      libraryDependencies <+= scalaVersion(
        "org.scala-lang" % "scala-compiler" % _
      )
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git#259d7e17a9c7aa72dec9abe8c0bb61ea9e49e3bd"), "banana-sesame")
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
      /*"org.w3" %% "banana-rdf" % "0.4",
      "org.w3" %% "banana-jena" % "0.4",
      "org.w3" %% "banana-sesame" % "0.4",*/
      "org.slf4j" % "slf4j-simple" % "1.6.4",
      "org.typelevel" %% "scalaz-contrib-210" % "0.1.4"
    )
  )
}

