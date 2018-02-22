package eu.phisikus.plotka.conf.providers

import java.util.Optional

import com.orbitz.consul.Consul
import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider}
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read

/**
  * This configuration provider uses Consul as a source of settings.
  *
  * @param consulUrl URL that will be used for HTTP API calls to consul
  * @param consulKey key that will be used while retrieving settings information
  */
class ConsulConfigurationProvider(val consulUrl: String,
                                  val consulKey: String) extends NodeConfigurationProvider {
  private implicit val formats = Serialization.formats(NoTypeHints)
  private val consulClient: Consul = Consul
    .builder()
    .withUrl(consulUrl)
    .build()

  override def loadConfiguration: NodeConfiguration = {
    val keyValueClient = consulClient.keyValueClient()
    val settingsAsString: Optional[String] = keyValueClient.getValueAsString(consulKey)
    if (!settingsAsString.isPresent) {
      throw new RuntimeException("Could not load configuration from consul for given key!")
    }
    read[BasicNodeConfiguration](settingsAsString.get())
  }
}
