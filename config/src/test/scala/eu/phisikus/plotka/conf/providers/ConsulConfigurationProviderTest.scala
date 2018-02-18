package eu.phisikus.plotka.conf.providers

import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import org.scalatest.{BeforeAndAfter, FlatSpec}

class ConsulConfigurationProviderTest extends FlatSpec with BeforeAndAfter {

  lazy val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()

  before {
    testConsul
  }

  after {
    testConsul.close()
  }

  "Configuration file" should "be loaded from consul" in {
    val consulUrl = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort
    val testConfigurationProvider = new ConsulConfigurationProvider(consulUrl, "plotka-test")
    testConfigurationProvider.loadConfiguration
  }

}
