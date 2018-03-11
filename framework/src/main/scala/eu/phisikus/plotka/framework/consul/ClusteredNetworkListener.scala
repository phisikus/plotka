package eu.phisikus.plotka.framework.consul

import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.model.{NetworkMessageConsumer, NetworkPeer}
import eu.phisikus.plotka.network.listener.{ListenerController, NetworkListener}

/**
  * This is a [[NetworkListener]] implementation with additional consul-based node discovery capabilities.
  * It registers as a service in the Consul on [[ListenerController.start()]] and unregisters on [[ListenerController.stop()]].
  * Additionally you can get a set of other available instances of the service by calling [[getPeers()]].
  *
  * @param nodeConfiguration initial configuration
  * @param messageConsumer   handler that will be called for each received message
  * @param registryManager   consul registry manager that will be used to register and unregister the service
  */
class ClusteredNetworkListener(nodeConfiguration: NodeConfiguration,
                               messageConsumer: NetworkMessageConsumer,
                               registryManager: ConsulServiceRegistryManager)
  extends NetworkListener(nodeConfiguration, messageConsumer) {

  override def start(): Unit = {
    registryManager.register()
    super.start()
  }

  override def stop(): Unit = {
    registryManager.unregister()
    super.stop()
    registryManager.shutdown()
  }

  /**
    * Get information about active peers
    *
    * @return set of NetworkPeer objects representing active peers
    */
  def getPeers(): Set[NetworkPeer] = {
    registryManager
      .getPeers()
      .filter(peer => peer.id != nodeConfiguration.id)
  }

}
