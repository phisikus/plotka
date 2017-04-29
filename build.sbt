name := "plotka"

version := "0.0.1"

scalaVersion := "2.12.2"

publishMavenStyle := true

mainClass in Compile := Some("pl.weimaraner.plotka.EntryPoint")

libraryDependencies ++= Seq(
  // Configuration
  "com.typesafe" % "config" % "1.3.1",

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

  // Testing
  "org.scalatest" % "scalatest_2.12" % "3.0.1" % Test
)

