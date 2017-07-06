package pl.weimaraner.plotka.network.listener.handlers

import pl.weimaraner.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer, Peer}

import scala.collection.mutable

class QueueMessageHandler extends NetworkMessageConsumer {
  val receivedMessages: mutable.MutableList[Message[NetworkPeer, Peer, Serializable]] = mutable.MutableList()

  override def consumeMessage(message: Message[NetworkPeer, Peer, Serializable]): Unit = {
    receivedMessages += message
  }
}
