import java.util.Properties

import sbt.Keys._
import sbt.{Def, _}
import scoverage.ScoverageKeys.{coverageEnabled, coverageFailOnMinimum, coverageMinimum}

object Common {
  val settings = Seq(
    version := getProjectVersion,
    organization := "eu.phisikus",
    scalaVersion := "2.12.2",
    publishMavenStyle := true
  )

  private def getProjectVersion = {
    val buildProperties = new Properties()
    IO.load(buildProperties, new File("project/build.properties"))
    buildProperties.getProperty("project.version")
  }

  val testCoverageSettings = Seq(
    coverageMinimum := 70,
    coverageFailOnMinimum := true
  )

  val dependencies: Def.Setting[Seq[ModuleID]] = libraryDependencies ++= Seq(
    Dependencies.commons,
    Dependencies.configuration,
    Dependencies.logging,
    Dependencies.testing
  ).flatten

  object Dependencies {
    val commons = Seq(
      "org.apache.commons" % "commons-lang3" % "3.6"
    )

    val configuration = Seq(
      "com.typesafe" % "config" % "1.3.1"
    )

    val logging = Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
    )

    val testing = Seq(
      "org.scalatest" % "scalatest_2.12" % "3.0.1" % Test
    )
  }

  val publishingSettings = Seq(
    publishTo := Some(Resolver.sftp("Phisikus Maven Repo","phisikus.eu","domains/phisikus.eu/public_html/maven2")),
    publishArtifact in Test := false,
    isSnapshot := true
  )


}
