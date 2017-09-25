package eu.phisikus.plotka.model

sealed trait MessageConsumer[S, R, M <: Serializable] {
  def consumeMessage(message: Message[S, R, M])
}

/**
  * NetworkMessageConsumer is used by NetworkListener as an incoming message handler.
  */
trait NetworkMessageConsumer extends MessageConsumer[NetworkPeer, Peer, Serializable]

