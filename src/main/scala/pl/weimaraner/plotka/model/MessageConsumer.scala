package pl.weimaraner.plotka.model

sealed trait MessageConsumer[S, R, M <: Serializable] {
  def consumeMessage(message: Message[S, R, M])
}

/**
  * MessageConsumer is used by Listener as an incoming message handler.
  */
trait NetworkMessageConsumer extends MessageConsumer[NetworkPeer, Peer, Serializable]

