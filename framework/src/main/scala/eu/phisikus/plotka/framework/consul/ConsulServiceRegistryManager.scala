package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.{ImmutableRegistration, Registration}
import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.model.NetworkPeer

import scala.collection.JavaConverters._

/**
  * This class manages the registration of service based on NodeConfiguration
  *
  * @param consulUrl         URL of the consul agent
  * @param serviceName       name of the service
  * @param nodeConfiguration node configuration providing meta-data about the instance of the service
  * @param consulBuilder     optional builder for consul client that you can set up with additional options
  */
class ConsulServiceRegistryManager(consulUrl: String,
                                   serviceName: String,
                                   nodeConfiguration: NodeConfiguration,
                                   consulBuilder: Consul.Builder = Consul.builder()) {
  private val consul: Consul = consulBuilder
    .withUrl(consulUrl)
    .build()
  private val consulAgentClient = consul.agentClient()
  private val consulHealthClient = consul.healthClient()

  /**
    * Register service in consul
    */
  def register(): Unit = {
    val registrationData: Registration = ImmutableRegistration.builder()
      .name(serviceName)
      .id(nodeConfiguration.id)
      .address(nodeConfiguration.address)
      .port(nodeConfiguration.port)
      .build()

    consulAgentClient.register(registrationData)
  }

  /**
    * Unregister service from consul
    */
  def unregister(): Unit = {
    consulAgentClient.deregister(nodeConfiguration.id)
  }

  /**
    * Get the information about services with the same name.
    *
    * @return set of services as NetworkPeers for easier connectivity
    */
  def getPeers(): Set[NetworkPeer] = {
    consulHealthClient
      .getHealthyServiceInstances(serviceName)
      .getResponse
      .asScala
      .map(serviceHealth => serviceHealth.getService)
      .map(service => NetworkPeer(service.getId, service.getAddress, service.getPort))
      .toSet
  }
}
