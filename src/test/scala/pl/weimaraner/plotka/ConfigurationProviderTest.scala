package pl.weimaraner.plotka

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite

class ConfigurationProviderTest extends FunSuite {
  test("Should load node settings") {
    val loadedConfiguration = ConfigFactory.load("test_configuration")
    assert(loadedConfiguration.hasPath("node"), "There is no configuration for node")
  }
}

