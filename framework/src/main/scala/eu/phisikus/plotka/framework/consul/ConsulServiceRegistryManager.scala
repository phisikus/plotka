package eu.phisikus.plotka.framework.consul

import java.util.concurrent.{Executors, TimeUnit}

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.{ImmutableRegCheck, ImmutableRegistration, Registration}
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

  private val healthCheckUpdateThread = Executors.newSingleThreadScheduledExecutor()
  private val healthCheckTimeout: Long = 30
  private val healthCheck = ImmutableRegCheck.builder()
    .ttl(healthCheckTimeout + "s")
    .build()


  /**
    * Register service in consul, should be called only once.
    */
  def register(): Unit = {
    val registrationData: Registration = ImmutableRegistration.builder()
      .name(serviceName)
      .id(nodeConfiguration.id)
      .address(nodeConfiguration.address)
      .port(nodeConfiguration.port)
      .check(healthCheck)
      .build()

    registerServiceAndStartHealthCheck(registrationData)
  }

  private def registerServiceAndStartHealthCheck(registrationData: Registration) = {
    val healthCheckRenewalProcedure: Runnable = () => {
      consulAgentClient.pass(registrationData.getId)
    }

    consulAgentClient.register(registrationData)
    healthCheckUpdateThread.scheduleAtFixedRate(
      healthCheckRenewalProcedure, 0L, healthCheckTimeout / 2, TimeUnit.SECONDS)
  }

  /**
    * Unregister service from consul, should be called only once
    */
  def unregister(): Unit = {
    healthCheckUpdateThread.shutdownNow()
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
