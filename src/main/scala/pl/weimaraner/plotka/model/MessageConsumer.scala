package pl.weimaraner.plotka.model

sealed trait MessageConsumer[S, R, M <: Serializable] {
  def consumeMessage(message: Message[S, R, M], sessionState: SessionState)
}

/**
  * MessageConsumer is used by Listener as an incoming message handler.
  * The caller will provide a message and a shared object created for each network connection.
  */
trait NetworkMessageConsumer extends MessageConsumer[NetworkPeer, Peer, Serializable]

