
lazy val config = project.in(file("config"))
  .settings(name := "plotka-config")
  .settings(Common.settings)
  .settings(Common.publishingSettings)
  .settings(Common.testCoverageSettings)
  .settings(Common.dependencies)

lazy val networking = project.in(file("networking"))
  .settings(name := "plotka-networking")
  .settings(Common.settings)
  .settings(Common.publishingSettings)
  .settings(Common.testCoverageSettings)
  .settings(Common.dependencies)
  .dependsOn(config)

lazy val plotka = project.in(file("."))
  .settings(name := "plotka")
  .settings(Common.settings)
  .settings(Common.publishingSettings)
  .aggregate(config, networking)
  .dependsOn(config, networking)



