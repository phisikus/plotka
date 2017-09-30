package eu.phisikus.plotka.conf

import com.typesafe.config.{Config, ConfigException}
import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider
import org.scalatest.{FunSuite, Matchers}

class FileConfigurationProviderTest extends FunSuite with Matchers {
  private val configurationFile = "test_configuration"
  private val customConfigurationFile = "custom_settings"
  private val expectedPeerConfiguration = List(
    BasicPeerConfiguration("node1.network", 2048),
    BasicPeerConfiguration("node2.network")
  )
  private val expectedConfiguration = BasicNodeConfiguration("node0.network", 2828, "10.0.0.1", expectedPeerConfiguration)


  test("Should load correct values from configuration file") {
    val fileConfigurationProvider = new FileConfigurationProvider(Some(configurationFile))
    val actualConfiguration = fileConfigurationProvider.loadConfiguration
    actualConfiguration should equal(expectedConfiguration)
  }

  test("Should fail when configuration file does not exist") {
    val fileConfigurationProvider = new FileConfigurationProvider()
    assertThrows[ConfigException.Missing] {
      fileConfigurationProvider.loadConfiguration
    }
  }

  test("Should load configuration with custom settings") {
    val fileConfigurationProvider = new FileConfigurationProvider(Some(customConfigurationFile))
    val actualConfiguration = fileConfigurationProvider.loadConfiguration
    val settings = actualConfiguration.settings
    assertCustomSettings(settings)
  }

  private def assertCustomSettings(oSettings: Option[Config]): Any = {
    assert(oSettings.isDefined, "Expected 'settings' key to exist in the configuration")
    val settings = oSettings.get
    val users = settings.getConfigList("users")
    val firstUser = users.get(0)
    firstUser.getString("name") shouldEqual "John"
    firstUser.getString("surname") shouldEqual "Smith"
  }

}

