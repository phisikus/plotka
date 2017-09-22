package eu.phisikus.plotka.examples.ricart.agrawala


import java.util.concurrent.atomic.AtomicInteger

import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer
import eu.phisikus.plotka.network.listener.NetworkListener

class RicartAgrawalaNode(nodeConfiguration: NodeConfiguration) {
  val currentClock: AtomicInteger = new AtomicInteger(0)
  val myself = NetworkPeer(nodeConfiguration.id, nodeConfiguration.address, nodeConfiguration.port)
  val messageHandler = new StandardNetworkMessageConsumer(myself, (message, talker) => {

  })
  val nodeListener = new NetworkListener(nodeConfiguration, messageHandler)

  def start(): Unit = {
    nodeListener.start()
  }

  def stop(): Unit = {
    nodeListener.stop()
  }
}
