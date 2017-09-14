package eu.phisikus.plotka.examples.scala

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer
import eu.phisikus.plotka.network.listener.{NetworkListener, NetworkListenerBuilder}
import eu.phisikus.plotka.network.talker.NetworkTalker

import scala.collection.mutable


/**
  * This class loads configuration, sends "Hello" messages to all known peers.
  * It waits for responses from all of them and terminates.
  */
object EntryPoint {
  private val logger = Logger("EntryPoint")


  def main(args: Array[String]): Unit = {
    val configurationProvider: FileConfigurationProvider = new FileConfigurationProvider()
    val configuration = configurationProvider.loadConfiguration
    val peersThatResponded = new mutable.MutableList[NetworkPeer]

    val localPeer: NetworkPeer = NetworkPeer(configuration.id, configuration.address, configuration.port)
    val messageConsumer = new StandardNetworkMessageConsumer(localPeer, (message, talker) => {
      val textMessage = message.getMessage.asInstanceOf[TextMessage]
      val sender = message.sender.asInstanceOf[NetworkPeer]
      val receivedText = textMessage.text
      logger.info("I have received message from {} with content = {}", sender, receivedText)
      peersThatResponded += sender
    })

    val testListener: NetworkListener = buildListener(localPeer, messageConsumer)

    testListener.start()

    sendHelloToAllPeers(configuration, localPeer)
    waitForAllResponses(configuration, peersThatResponded)
    val listOfPeersThatResponded = peersThatResponded.map(localPeer => localPeer.id).mkString(",")
    logger.info("Received replies from: {}", listOfPeersThatResponded)

    testListener.stop()

  }

  private def waitForAllResponses(configuration: NodeConfiguration, peersThatResponded: mutable.MutableList[NetworkPeer]) = {
    val availablePeers = configuration.peers
    repeatWhileWithDelay {
      peersThatResponded.length < availablePeers.length
    }

    val allResponsesReceived = availablePeers.forall(peerConf => peersThatResponded.exists(
      peer => {
        peer.address == peerConf.address && peer.port == peerConf.port
      }))
    assert(allResponsesReceived, "Not all nodes responded!")

  }


  private def buildListener(localPeer: NetworkPeer, messageConsumer: StandardNetworkMessageConsumer) = {
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
