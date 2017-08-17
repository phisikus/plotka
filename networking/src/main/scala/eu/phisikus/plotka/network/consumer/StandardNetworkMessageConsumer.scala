package eu.phisikus.plotka.network.consumer

import eu.phisikus.plotka.model._
import eu.phisikus.plotka.network.talker.{NetworkTalker, Talker}

/**
  * This basic network message consumer composes NetworkTalker to allow
  * communication in both directions. The localPeer definition is
  * used as a sender identification during messaging. Message handler
  * is provided with incoming message and also instance of Talker
  * that can be used to send a response.
  *
  * @param localPeer      local peer definition used by talker
  * @param messageHandler function to be executed when message arrives
  */
class StandardNetworkMessageConsumer(localPeer: Peer,
                                     messageHandler: (NetworkMessage, Talker) => Unit)
  extends NetworkMessageConsumer {
  private val talker: Talker = new NetworkTalker(localPeer)

  override def consumeMessage(message: Message[NetworkPeer, Peer, Serializable]): Unit = {
    consumeNetworkMessage(message.asInstanceOf[NetworkMessage])
  }

  private def consumeNetworkMessage(message: NetworkMessage): Unit = {
    messageHandler.apply(message, talker)
  }
}
