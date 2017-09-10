package eu.phisikus.plotka.examples.scala

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.model.{NetworkMessage, NetworkPeer}
import eu.phisikus.plotka.network.consumer.StandardNetworkMessageConsumer
import eu.phisikus.plotka.network.listener.NetworkListenerBuilder
import eu.phisikus.plotka.network.talker.{NetworkTalker, Talker}

object EntryPoint {
  private val logger = Logger("EntryPoint")

  def main(args: Array[String]): Unit = {
    val localPeer = NetworkPeer("LocalPeer1", "127.0.0.1", 3030)
    val testTalker = new NetworkTalker(localPeer)

    val messageConsumer = new StandardNetworkMessageConsumer(localPeer, (message: NetworkMessage, talker: Talker) => {
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
    testTalker.send(localPeer, TextMessage("Hello!"))
    Thread.sleep(1000L)
    testListener.stop()

  }

}
