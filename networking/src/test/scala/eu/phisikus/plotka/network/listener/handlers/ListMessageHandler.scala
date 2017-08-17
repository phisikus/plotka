package eu.phisikus.plotka.network.listener.handlers

import eu.phisikus.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer, Peer}

import scala.collection.mutable

class ListMessageHandler extends NetworkMessageConsumer {
  val receivedMessages: mutable.MutableList[Message[NetworkPeer, Peer, Serializable]] = mutable.MutableList()

  override def consumeMessage(message: Message[NetworkPeer, Peer, Serializable]): Unit = {
    receivedMessages.synchronized({
      receivedMessages += message
    })
  }
}
