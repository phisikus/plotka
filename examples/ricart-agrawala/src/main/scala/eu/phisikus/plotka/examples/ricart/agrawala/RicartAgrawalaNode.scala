package eu.phisikus.plotka.examples.ricart.agrawala


import java.util.concurrent.atomic.AtomicLong

import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.examples.ricart.agrawala.message.{AcceptMessage, RequestMessage}
import eu.phisikus.plotka.model.{NetworkMessage, NetworkMessageConsumer, NetworkPeer, Peer}
import eu.phisikus.plotka.network.listener.NetworkListener
import eu.phisikus.plotka.network.talker.{NetworkTalker, Talker}

class RicartAgrawalaNode(nodeConfiguration: NodeConfiguration, forCriticalSectionExecution: () => Unit) {
  val currentClock: AtomicLong = new AtomicLong(0)
  val lastRequestClock: AtomicLong = new AtomicLong(-1)
  val numberOfAgreements: AtomicLong = new AtomicLong(0)
  val myself = NetworkPeer(nodeConfiguration.id, nodeConfiguration.address, nodeConfiguration.port)
  val talker = new NetworkTalker(myself)
  val messageHandler: NetworkMessageConsumer = inputMessage => {
    val msg = inputMessage.asInstanceOf[NetworkMessage]
    msg.message match {
      case request: RequestMessage =>
        processRequest(msg.sender, request.timestamp, talker)
      case agreement: AcceptMessage =>
        processAcceptance(msg.sender, agreement.timestamp, talker)

    }
  }

  private def processRequest(sender: Peer, requestTimestamp: Long, talker: Talker): Unit = {
    updateLocalClock(requestTimestamp)

  }

  private def processAcceptance(sender: Peer, acceptTimestamp: Long, talker: Talker): Unit = {
    updateLocalClock(acceptTimestamp)
  }


  private def updateLocalClock(timestamp: Long) = {
    currentClock.accumulateAndGet(timestamp, (currentValue, t) => Math.max(currentValue, t + 1))
  }


  val nodeListener = new NetworkListener(nodeConfiguration, messageHandler)

  def requestCriticalSection(): Unit = {
    val requestClockValue = currentClock.incrementAndGet()
    val requestMessage = RequestMessage(requestClockValue)
    lastRequestClock.set(requestClockValue)
    nodeConfiguration.peers.foreach(peer => {
      val recipient = peer.asInstanceOf[NetworkPeer]
      repeatWhileWithDelay({
        talker.send(recipient, requestMessage).isFailure
      })
    })
  }

  private def repeatWhileWithDelay(condition: => Boolean): Unit = {
    while (condition) {
      Thread.sleep(100L)
    }
  }

  def start(): Unit = {
    nodeListener.start()
    requestCriticalSection()
  }

  def stop(): Unit = {
    nodeListener.stop()
  }
}
