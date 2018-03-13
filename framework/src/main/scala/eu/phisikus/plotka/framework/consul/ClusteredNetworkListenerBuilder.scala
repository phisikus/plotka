package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.Consul
import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.conf.providers.ConsulConfigurationProvider
import eu.phisikus.plotka.framework.consul.consumer.{ClusterPeerListProvider, ClusteredNetworkMessageConsumer}
import eu.phisikus.plotka.model.{NetworkMessage, NetworkPeer}
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder
import eu.phisikus.plotka.network.talker.Talker

class ClusteredNetworkListenerBuilder extends NetworkListenerBuilder {
  protected var serviceName: String = "service"
  protected var consulUrl: String = "http://localhost:8500"
  protected var consulBuilder: Option[Consul.Builder] = None
  protected var advancedMessageConsumer: Option[(NetworkMessage, Talker, ClusterPeerListProvider) => Unit] = None

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

  def withAdvancedMessageHandler(messageHandler: (NetworkMessage, Talker, ClusterPeerListProvider) => Unit): this.type = {
    advancedMessageConsumer = Some(messageHandler)
    this
  }

  override def build(): ClusteredNetworkListener = {
    val registryManager = provideRegistryManager()
    val messageHandler = provideMessageHandler(registryManager)
    new ClusteredNetworkListener(nodeConfiguration, messageHandler, registryManager)
  }

  private def provideMessageHandler(registryManager: ConsulServiceRegistryManager) = {
    advancedMessageConsumer match {
      case None => messageConsumer
      case Some(handler) =>
        val localPeer = NetworkPeer(nodeConfiguration.id, nodeConfiguration.address, nodeConfiguration.port)
        new ClusteredNetworkMessageConsumer(localPeer, registryManager, handler)
    }
  }

  private def provideRegistryManager() = {
    consulBuilder match {
      case None => new ConsulServiceRegistryManager(consulUrl, serviceName, nodeConfiguration)
      case Some(consul) => new ConsulServiceRegistryManager(consulUrl, serviceName, nodeConfiguration, consul)
    }
  }
}

object ClusteredNetworkListenerBuilder {
  def apply(): ClusteredNetworkListenerBuilder = new ClusteredNetworkListenerBuilder()
}
