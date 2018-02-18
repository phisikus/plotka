package eu.phisikus.plotka.conf.providers

import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider}

/**
  * This configuration provider uses Consul as a source of settings.
  * @param consulUrl URL that will be used for HTTP API calls to consul
  * @param consulKeyPrefix prefix that will be used while retrieving settings information
  */
class ConsulConfigurationProvider(val consulUrl: String,
                                  val consulKeyPrefix: String) extends NodeConfigurationProvider {
  override def loadConfiguration: NodeConfiguration = ???
}
