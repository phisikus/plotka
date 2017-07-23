package pl.weimaraner.plotka.model

sealed abstract class Message[S, R, M <: Serializable](sender: S, recipient: R, message: M) extends Serializable

/**
  * This class represents message that can be sent and received over the network.
  *
  * @param sender    Peer object representing sender of the message
  * @param recipient Peer object representing recipient of the message
  * @param message   message body
  */
case class NetworkMessage(sender: Peer,
                          recipient: Peer,
                          message: Serializable)
  extends Message(sender = sender, recipient = recipient, message = message)

