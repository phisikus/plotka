name := "plotka"

version := "0.0.1"

scalaVersion := "2.12.2"

publishMavenStyle := true

mainClass in Compile := Some("pl.weimaraner.plotka.EntryPoint")

libraryDependencies ++= Seq(
	"org.scalatest" % "scalatest_2.12" % "3.0.1" % Test
)

