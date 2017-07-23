package pl.weimaraner.plotka.network.talker

import pl.weimaraner.plotka.model.NetworkPeer

/**
  * Talker is used to send messages to other peers over network
  */
trait Talker {
  /**
    * Sends message of given body to specified recipient
    *
    * @param recipient   recipient of the message
    * @param messageBody contents of the message
    */
  def send(recipient: NetworkPeer, messageBody: Serializable)

  /**
    * Shutdown all connections with peers
    */
  def shutdown()
}
