package pl.weimaraner.plotka.model

sealed abstract class Message[S, R, M <: Serializable](sender: S, recipient: R, message: M)

case class NetworkMessage(sender: Peer,
                          recipient: Peer,
                          message: Serializable)
  extends Message(sender = sender, recipient = recipient, message = message)

