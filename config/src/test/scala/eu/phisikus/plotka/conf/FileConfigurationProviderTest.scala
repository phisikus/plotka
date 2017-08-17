package eu.phisikus.plotka.conf

import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider
import org.scalatest.{FunSuite, Matchers}

class FileConfigurationProviderTest extends FunSuite with Matchers {
  private val configurationFile = "test_configuration"
  private val expectedPeerConfiguration = List(
    BasicPeerConfiguration("node1.network", 2048),
    BasicPeerConfiguration("node2.network")
  )
  private val expectedConfiguration = BasicNodeConfiguration("node0.network", 2828, "10.0.0.1", expectedPeerConfiguration)

  test("Should load correct values from configuration file") {
    val fileConfigurationProvider = new FileConfigurationProvider(configurationFile)
    val actualConfiguration = fileConfigurationProvider.loadConfiguration
    actualConfiguration should equal(expectedConfiguration)
  }
}

