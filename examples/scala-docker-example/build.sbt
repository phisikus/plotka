name := "scala-docker-example"

organization := "eu.phisikus"

version := "0.0.1"

scalaVersion := "2.12.2"

publishMavenStyle := true

mainClass in Compile := Some("eu.phisikus.plotka.examples.scala.EntryPoint")

resolvers ++= Seq(
  "Phisikus' Maven Repository" at "http://phisikus.eu/maven2"
)

libraryDependencies ++= Seq(
  "eu.phisikus" % "plotka_2.12" % "0.0.1"
)

enablePlugins(DockerPlugin)

imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest"),

  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  )
)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}