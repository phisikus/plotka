package eu.phisikus.plotka.conf.providers

import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}

/**
  * Contains values matching the example test configuration files
  */
private[providers] object TestConfigurationExamples {
  val ConfigurationFile = "test_configuration"
  val CustomConfigurationFile = "custom_settings"
  val ExpectedPeerConfiguration = List(
    BasicPeerConfiguration("node1.network", 2048),
    BasicPeerConfiguration("node2.network")
  )
  val ExpectedConfiguration = BasicNodeConfiguration("node0.network", 2828, "10.0.0.1", ExpectedPeerConfiguration)

}
