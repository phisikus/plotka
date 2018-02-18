package eu.phisikus.plotka.conf.providers

import com.google.gson.Gson
import com.orbitz.consul.Consul
import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider}

/**
  * This configuration provider uses Consul as a source of settings.
  *
  * @param consulUrl URL that will be used for HTTP API calls to consul
  * @param consulKey key that will be used while retrieving settings information
  */
class ConsulConfigurationProvider(val consulUrl: String,
                                  val consulKey: String) extends NodeConfigurationProvider {
  private val consulClient: Consul = Consul
    .builder()
    .withUrl(consulUrl)
    .build()

  private val jsonConverter: Gson = new Gson()

  override def loadConfiguration: NodeConfiguration = {
    val keyValueClient = consulClient.keyValueClient()
    val settingsAsString = keyValueClient.getValueAsString(consulKey)
    ???
  }
}
