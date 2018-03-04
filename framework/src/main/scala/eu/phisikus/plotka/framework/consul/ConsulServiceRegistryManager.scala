package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.{ImmutableRegistration, Registration}
import eu.phisikus.plotka.conf.NodeConfiguration

/**
  * This class manages the registration of service based on NodeConfiguration
  *
  * @param consulUrl         URL of the consul agent
  * @param serviceName       name of the service
  * @param nodeConfiguration node configuration providing meta-data about the service
  * @param consulBuilder     optional builder for consul client that you can set up with additional options
  */
class ConsulServiceRegistryManager(consulUrl: String,
                                   serviceName: String,
                                   nodeConfiguration: NodeConfiguration,
                                   consulBuilder: Consul.Builder = Consul.builder()) {
  private val consulAgentClient = consulBuilder
    .withUrl(consulUrl)
    .build()
    .agentClient()

  def register(): Unit = {
    val registrationData: Registration = ImmutableRegistration.builder()
      .name(serviceName)
      .id(nodeConfiguration.id)
      .address(nodeConfiguration.address)
      .port(nodeConfiguration.port)
      .build()

    consulAgentClient.register(registrationData)
  }

  def unregister(): Unit = {
    consulAgentClient.deregister(nodeConfiguration.id)
  }
}
