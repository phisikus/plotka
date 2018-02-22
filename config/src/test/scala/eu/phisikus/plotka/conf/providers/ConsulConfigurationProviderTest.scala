package eu.phisikus.plotka.conf.providers

import com.orbitz.consul.Consul
import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ConsulConfigurationProviderTest extends FlatSpec with BeforeAndAfter with Matchers {
  private implicit val formats = Serialization.formats(NoTypeHints)
  private lazy val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()

  private val testConfigurationKey = "plotka-test"

  before {
    testConsul
  }

  after {
    testConsul.close()
  }

  "Configuration file" should "be loaded from consul" in {
    val consulUrl = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort
    val testConfigurationProvider = new ConsulConfigurationProvider(consulUrl, testConfigurationKey)

    val expectedConfiguration = BasicNodeConfiguration(peers = List())
    putConfigurationInConsul(expectedConfiguration, consulUrl)

    val actualConfiguration = testConfigurationProvider.loadConfiguration
    actualConfiguration should equal(expectedConfiguration)
  }

  private def putConfigurationInConsul(expectedConfiguration: BasicNodeConfiguration, consulUrl: String) = {
    Consul
      .builder()
      .withUrl(consulUrl)
      .build()
      .keyValueClient()
      .putValue(testConfigurationKey, write(expectedConfiguration))
  }
}
