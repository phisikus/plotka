package eu.phisikus.plotka.examples.scala

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder
import eu.phisikus.plotka.network.talker.NetworkTalker

object EntryPoint {
  private val logger = Logger("EntryPoint")

  def main(args: Array[String]): Unit = {
    val configurationProvider: FileConfigurationProvider = new FileConfigurationProvider()
    val configuration = configurationProvider.loadConfiguration
    val peerConfiguration = configuration.peers.head
    val localPeer: NetworkPeer = NetworkPeer(configuration.id, configuration.address, configuration.port)

    val messageConsumer = new StandardNetworkMessageConsumer(localPeer, (message, talker) => {
      val textMessage = message.getMessage.asInstanceOf[TextMessage]
      val receivedText = textMessage.text
      logger.info("All good! I've got the message: {}", receivedText)
    })

    val testListener = NetworkListenerBuilder()
      .withId(localPeer.id)
      .withAddress(localPeer.address)
      .withPort(localPeer.port)
      .withMessageHandler(messageConsumer)
      .build()


    testListener.start()
    val targetPeer = new NetworkPeer(peerConfiguration.address, peerConfiguration.port)
    val testTalker = new NetworkTalker(localPeer)
    testTalker.send(localPeer, TextMessage("Hello!"))
    Thread.sleep(1000L)
    testListener.stop()

  }

}
