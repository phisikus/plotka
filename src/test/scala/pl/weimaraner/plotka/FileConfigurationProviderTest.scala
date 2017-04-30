package pl.weimaraner.plotka

import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import pl.weimaraner.plotka.conf.providers.FileConfigurationProvider

class FileConfigurationProviderTest extends FunSuite with Matchers {
  private val configurationFile = "test_configuration"
  private val expectedPeerConfiguration = List(
    new BasicPeerConfiguration("node1.network", 2048),
    new BasicPeerConfiguration("node2.network")
  )
  private val expectedConfiguration = new BasicNodeConfiguration("node0.network", 2828, "10.0.0.1", expectedPeerConfiguration)

  test("Should load correct values from configuration file") {
    val fileConfigurationProvider = new FileConfigurationProvider("test_configuration")
    val actualConfiguration = fileConfigurationProvider.loadConfiguration
    actualConfiguration should equal (expectedConfiguration)
  }
}

