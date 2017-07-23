package pl.weimaraner.plotka.network.talker

import pl.weimaraner.plotka.model.NetworkPeer

import scala.util.Try

/**
  * Talker is used to send messages to other peers over network
  */
trait Talker {
  /**
    * Sends message of given body to specified recipient
    *
    * @param recipient   recipient of the message
    * @param messageBody contents of the message
    * @return result of the operation (Success/Failure)
    */
  def send(recipient: NetworkPeer, messageBody: Serializable) : Try[Unit]

  /**
    * Shutdown all connections with peers
    */
  def shutdown()
}
