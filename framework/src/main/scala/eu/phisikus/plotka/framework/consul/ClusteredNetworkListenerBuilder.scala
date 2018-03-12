package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.Consul
import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.conf.providers.ConsulConfigurationProvider
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder

class ClusteredNetworkListenerBuilder extends NetworkListenerBuilder {
  protected var serviceName: String = "service"
  protected var consulUrl: String = "http://localhost:8500"
  protected var consulBuilder: Option[Consul.Builder] = None

  def withServiceName(serviceName: String): this.type = {
    this.serviceName = serviceName
    this
  }

  def withConsulUrl(consulUrl: String): this.type = {
    this.consulUrl = consulUrl
    this
  }

  def withConsulBuilder(consulBuilder: Consul.Builder): this.type = {
    this.consulBuilder = Some(consulBuilder)
    this
  }

  def withConsulNodeConfiguration(consulKey: String): this.type = {
    val configurationProvider = consulBuilder match {
      case None => new ConsulConfigurationProvider(consulUrl, consulKey)
      case Some(builder) => new ConsulConfigurationProvider(consulUrl, consulKey, builder)
    }

    nodeConfiguration = configurationProvider.loadConfiguration.asInstanceOf[BasicNodeConfiguration]
    this
  }

  override def build(): ClusteredNetworkListener = {
    val registryManager = consulBuilder match {
      case None => new ConsulServiceRegistryManager(consulUrl, serviceName, nodeConfiguration)
      case Some(consul) => new ConsulServiceRegistryManager(consulUrl, serviceName, nodeConfiguration, consul)
    }
    new ClusteredNetworkListener(nodeConfiguration, messageConsumer, registryManager)
  }

}

object ClusteredNetworkListenerBuilder {
  def apply(): ClusteredNetworkListenerBuilder = new ClusteredNetworkListenerBuilder()
}
