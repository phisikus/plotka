package eu.phisikus.plotka.framework.consul.consumer

import eu.phisikus.plotka.framework.consul.{ClusteredNetworkListener, ConsulServiceRegistryManager}
import eu.phisikus.plotka.model.NetworkPeer

/**
  * It provides current list of active peers in the cluster.
  * @param registryManager service registry manager that can provide active peer information
  */
class ClusterPeerListProvider(registryManager: ConsulServiceRegistryManager)
  extends (() => Set[NetworkPeer]) {
  override def apply(): Set[NetworkPeer] = {
    registryManager.getPeers()
  }
}
