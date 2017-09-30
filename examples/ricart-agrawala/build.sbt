name := "ricart-agrawala"

organization := "eu.phisikus"

version := "0.0.1"

scalaVersion := "2.12.2"

publishMavenStyle := true

mainClass in Compile := Some("eu.phisikus.plotka.examples.ricart.agrawala.EntryPoint")

resolvers ++= Seq(
  "Phisikus' Maven Repository" at "http://phisikus.eu/maven2"
)

libraryDependencies ++= Seq(
  "eu.phisikus" % "plotka_2.12" % "0.0.3",
  "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test"
)
