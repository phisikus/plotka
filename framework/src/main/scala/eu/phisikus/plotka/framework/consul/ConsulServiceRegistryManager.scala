package eu.phisikus.plotka.framework.consul

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.{ImmutableRegCheck, ImmutableRegistration, Registration}
import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.model.NetworkPeer

import scala.collection.JavaConverters._

/**
  * This class manages the registration of service based on NodeConfiguration
  *
  * @param consulUrl          URL of the consul agent
  * @param serviceName        name of the service
  * @param nodeConfiguration  node configuration providing meta-data about the instance of the service
  * @param consulBuilder      optional builder for consul client that you can set up with additional options
  * @param healthCheckTimeout timeout (in seconds) used for service health check (default = 30s)
  */
class ConsulServiceRegistryManager(consulUrl: String,
                                   serviceName: String,
                                   nodeConfiguration: NodeConfiguration,
                                   consulBuilder: Consul.Builder = Consul.builder(),
                                   healthCheckTimeout: Long = 30) {
  private val consul: Consul = consulBuilder
    .withUrl(consulUrl)
    .build()
  private val consulAgentClient = consul.agentClient()
  private val consulHealthClient = consul.healthClient()

  private val healthCheckUpdateExecutor: ScheduledThreadPoolExecutor = buildHealthCheckThreadPool
  private val healthCheckUpdateTask = new AtomicReference[Option[ScheduledFuture[_]]](None)
  private val healthCheck = ImmutableRegCheck.builder()
    .ttl(healthCheckTimeout + "s")
    .build()

  /**
    * Register service in consul and manage the health checks
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

  private def registerServiceAndStartHealthCheck(registrationData: Registration): Unit = {
    consulAgentClient.register(registrationData)
    startHealthCheckUpdates(registrationData.getId)
  }

  private def startHealthCheckUpdates(nodeId : String): Unit = {
    val healthCheckRenewalProcedure: Runnable = () => {
      consulAgentClient.pass(nodeId)
    }

    if (healthCheckUpdateTask.get().isEmpty) {
      val updateTask = healthCheckUpdateExecutor.scheduleAtFixedRate(
        healthCheckRenewalProcedure, 0L, healthCheckTimeout / 2, TimeUnit.SECONDS
      )
      healthCheckUpdateTask.set(Some(updateTask))
    }
  }

  /**
    * Unregister service from consul, should be called only once
    */
  def unregister(): Unit = {
    stopHealthCheckUpdates()
    consulAgentClient.deregister(nodeConfiguration.id)
  }

  private def stopHealthCheckUpdates() = {
    val updateTask = healthCheckUpdateTask.getAndSet(None)
    updateTask.map(task => task.cancel(true))
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

  private def buildHealthCheckThreadPool = {
    val threadPool = Executors
      .newScheduledThreadPool(1)
      .asInstanceOf[ScheduledThreadPoolExecutor]
    threadPool.setRemoveOnCancelPolicy(true)
    threadPool
  }

  /**
    * Stop thread pools, free resources.
    */
  def shutdown() : Unit = {
    consul.destroy()
    buildHealthCheckThreadPool.shutdownNow()
  }
}
