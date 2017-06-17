package pl.weimaraner.plotka.model

sealed trait MessageConsumer[S, R, M] {
  def consumeMessage(message: Message[S, R, M])
}

trait NetworkMessageConsumer extends MessageConsumer[NetworkPeer, NetworkPeer, Serializable]

