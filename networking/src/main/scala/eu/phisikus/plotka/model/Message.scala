package eu.phisikus.plotka.model

import scala.beans.BeanProperty

sealed abstract class Message[S, R, M <: Serializable](sender: S, recipient: R, message: M) extends Serializable

/**
  * This class represents message that can be sent and received over the network.
  *
  * @param sender    Peer object representing sender of the message
  * @param recipient Peer object representing recipient of the message
  * @param message   message body
  */
case class NetworkMessage(@BeanProperty sender: Peer,
                          @BeanProperty recipient: Peer,
                          @BeanProperty message: Serializable)
  extends Message(sender = sender, recipient = recipient, message = message)

