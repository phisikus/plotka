package eu.phisikus.plotka.conf.providers

import com.orbitz.consul.Consul
import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import com.typesafe.config.{ConfigException, ConfigFactory}
import eu.phisikus.plotka.conf.mappers.ConfigToNodeConfigurationMapper
import eu.phisikus.plotka.conf.providers.TestConfigurationExamples.TestCustomConfigurationText
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class ConsulConfigurationProviderTest extends FunSuite with Matchers with BeforeAndAfterAll {
  private val configurationMapper = new ConfigToNodeConfigurationMapper()
  private val expectedCustomConfiguration = configurationMapper.map(ConfigFactory.parseString(TestCustomConfigurationText))
  private lazy val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()

  private val testConfigurationKey = "plotka-test"
  private val fakeConfigurationKey = "plotka-test-fake"
  private val consulUrl = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort

  override protected def beforeAll(): Unit = {
    putConfigurationInConsul(TestCustomConfigurationText, consulUrl)
  }

  override protected def afterAll(): Unit = {
    testConsul.close()
  }


  test("Should load configuration file from consul") {
    val testConfigurationProvider = new ConsulConfigurationProvider(consulUrl, testConfigurationKey)
    val actualConfiguration = testConfigurationProvider.loadConfiguration
    actualConfiguration should equal(expectedCustomConfiguration)
  }

  test("Should not load configuration file from nonexisting key in consul") {
    val testConfigurationProvider = new ConsulConfigurationProvider(consulUrl, fakeConfigurationKey)
    assertThrows[ConfigException.Missing] {
      testConfigurationProvider.loadConfiguration
    }
  }


  private def putConfigurationInConsul(expectedConfiguration: String, consulUrl: String) = {
    Consul
      .builder()
      .withUrl(consulUrl)
      .build()
      .keyValueClient()
      .putValue(testConfigurationKey, expectedConfiguration)
  }
}
