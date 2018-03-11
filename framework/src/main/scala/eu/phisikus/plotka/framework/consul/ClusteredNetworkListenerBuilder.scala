package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.Consul
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
