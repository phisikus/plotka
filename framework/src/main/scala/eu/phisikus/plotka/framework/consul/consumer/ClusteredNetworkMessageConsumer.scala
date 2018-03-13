package eu.phisikus.plotka.framework.consul.consumer

import eu.phisikus.plotka.framework.consul.ConsulServiceRegistryManager
import eu.phisikus.plotka.model.{NetworkMessage, NetworkPeer}
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer
import eu.phisikus.plotka.network.talker.Talker

/**
  * This network message consumer composes inherited NetworkTalker to allow
  * communication in both directions. Message handler is provided with
  * incoming [[NetworkMessage]], instance of [[Talker]] that can be used to
  * send a response and [[ClusterPeerListProvider]] which provides list of
  * currently active peers.
  *
  * @param localPeer                    local peer definition used by talker
  * @param consulServiceRegistryManager service registry manager that provides list of active peers
  * @param messageHandler               function to be executed when message arrives
  */
class ClusteredNetworkMessageConsumer(localPeer: NetworkPeer,
                                      consulServiceRegistryManager: ConsulServiceRegistryManager,
                                      messageHandler: (NetworkMessage, Talker, ClusterPeerListProvider) => Unit)
  extends StandardNetworkMessageConsumer(
    localPeer,
    (message, talker) => messageHandler.apply(message, talker, new ClusterPeerListProvider(consulServiceRegistryManager))
  )