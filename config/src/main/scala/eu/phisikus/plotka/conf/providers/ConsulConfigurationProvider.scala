package eu.phisikus.plotka.conf.providers

import java.util.Optional

import com.orbitz.consul.Consul
import com.typesafe.config.{ConfigException, ConfigFactory}
import eu.phisikus.plotka.conf.mappers.ConfigToNodeConfigurationMapper
import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider}

/**
  * This configuration provider uses Consul as a source of settings.
  *
  * @param consulUrl URL that will be used for HTTP API calls to consul
  * @param consulKey key that will be used while retrieving settings information
  */
class ConsulConfigurationProvider(val consulUrl: String,
                                  val consulKey: String) extends NodeConfigurationProvider {
  private val nodeConfigurationMapper = new ConfigToNodeConfigurationMapper
  private val consulClient: Consul = Consul
    .builder()
    .withUrl(consulUrl)
    .build()

  override def loadConfiguration: NodeConfiguration = {
    val keyValueClient = consulClient.keyValueClient()
    val settingsAsString: Optional[String] = keyValueClient.getValueAsString(consulKey)
    if (!settingsAsString.isPresent) {
      throw new ConfigException.Missing(consulKey)
    }
    val config = ConfigFactory.parseString(settingsAsString.get())
    nodeConfigurationMapper.map(config)
  }
}
