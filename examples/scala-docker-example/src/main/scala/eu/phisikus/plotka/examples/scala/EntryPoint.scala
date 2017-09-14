package eu.phisikus.plotka.examples.scala

import java.util.concurrent.ConcurrentLinkedQueue

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider
import eu.phisikus.plotka.model.{NetworkMessage, NetworkMessageConsumer, NetworkPeer}
import eu.phisikus.plotka.network.listener.{NetworkListener, NetworkListenerBuilder}
import eu.phisikus.plotka.network.talker.NetworkTalker


/**
  * This class loads configuration, sends "Hello" messages to all known peers.
  * It waits for responses from all of them and terminates.
  */
object EntryPoint {
  private val logger = Logger("EntryPoint")
  private val peersThatResponded = new ConcurrentLinkedQueue[NetworkPeer]

  /**
    * Main logic here - incoming message handler stores the sender info in the queue
    */
  private val messageConsumer: NetworkMessageConsumer = incomingMessage => {
    val message = incomingMessage.asInstanceOf[NetworkMessage]
    val textMessage = message.getMessage.asInstanceOf[TextMessage]
    val sender = message.sender.asInstanceOf[NetworkPeer]
    val receivedText = textMessage.text
    logger.info("I have received message from {} with content = {}", sender, receivedText)
    peersThatResponded.add(sender)
  }


  def main(args: Array[String]): Unit = {
    val configuration: NodeConfiguration = getConfiguration
    val localPeer: NetworkPeer = NetworkPeer(configuration.id, configuration.address, configuration.port)

    val testListener: NetworkListener = buildListener(localPeer, messageConsumer)

    testListener.start()

    sendHelloToAllPeers(configuration, localPeer)
    waitForAllResponses(configuration, peersThatResponded)

    printRespondingPeerList()

    testListener.stop()

  }

  private def printRespondingPeerList() = {
    val listOfPeersThatResponded = peersThatResponded
      .toArray(Array[NetworkPeer]())
      .map(peer => peer.id)
      .mkString(",")
    logger.info("Received replies from: {}", listOfPeersThatResponded)
  }

  private def getConfiguration = {
    val configurationProvider: FileConfigurationProvider = new FileConfigurationProvider()
    val configuration = configurationProvider.loadConfiguration
    configuration
  }

  private def waitForAllResponses(configuration: NodeConfiguration, peersThatResponded: ConcurrentLinkedQueue[NetworkPeer]) = {
    val availablePeers = configuration.peers
    repeatWhileWithDelay {
      peersThatResponded.size() < availablePeers.length
    }
    val allResponsesReceived = availablePeers.forall(peerConf => peersThatResponded
      .stream()
      .anyMatch(peer => {
        peer.address == peerConf.address && peer.port == peerConf.port
      }))
    assert(allResponsesReceived, "Not all nodes responded!")

  }


  private def buildListener(localPeer: NetworkPeer, messageConsumer: NetworkMessageConsumer) = {
    val testListener = NetworkListenerBuilder()
      .withId(localPeer.id)
      .withAddress(localPeer.address)
      .withPort(localPeer.port)
      .withMessageHandler(messageConsumer)
      .build()
    testListener
  }

  private def sendHelloToAllPeers(configuration: NodeConfiguration, localPeer: NetworkPeer) = {
    val testTalker = new NetworkTalker(localPeer)
    configuration.peers.foreach(peerConfiguration => {
      val targetPeer = new NetworkPeer(peerConfiguration.address, peerConfiguration.port)
      repeatWhileWithDelay {
        testTalker.send(targetPeer, TextMessage("Hello!")).isFailure
      }
    })
    testTalker.shutdown()
  }

  private def repeatWhileWithDelay(condition: => Boolean): Unit = {
    while (condition) {
      Thread.sleep(100L)
    }
  }
}
