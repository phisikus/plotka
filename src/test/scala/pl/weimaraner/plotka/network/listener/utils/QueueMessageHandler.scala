package pl.weimaraner.plotka.network.listener.utils

import pl.weimaraner.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer, Peer}

import scala.collection.mutable

class QueueMessageHandler extends NetworkMessageConsumer {
  val receivedMessages: mutable.Queue[Message[NetworkPeer, Peer, Serializable]] = mutable.Queue()

  override def consumeMessage(message: Message[NetworkPeer, Peer, Serializable]): Unit = {
    receivedMessages.enqueue(message)
  }
}
