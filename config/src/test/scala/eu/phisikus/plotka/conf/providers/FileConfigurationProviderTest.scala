package eu.phisikus.plotka.conf.providers

import com.typesafe.config.{Config, ConfigException}
import org.scalatest.{FunSuite, Matchers}
import TestConfigurationExamples._

class FileConfigurationProviderTest extends FunSuite with Matchers {

  test("Should load correct values from configuration file") {
    val fileConfigurationProvider = new FileConfigurationProvider(Some(ConfigurationFile))
    val actualConfiguration = fileConfigurationProvider.loadConfiguration
    actualConfiguration should equal(ExpectedConfiguration)
  }

  test("Should fail when configuration file does not exist") {
    val fileConfigurationProvider = new FileConfigurationProvider()
    assertThrows[ConfigException.Missing] {
      fileConfigurationProvider.loadConfiguration
    }
  }

  test("Should load configuration with custom settings") {
    val fileConfigurationProvider = new FileConfigurationProvider(Some(CustomConfigurationFile))
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

