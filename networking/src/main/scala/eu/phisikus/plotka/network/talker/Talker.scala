package eu.phisikus.plotka.network.talker

import eu.phisikus.plotka.model.NetworkPeer

import scala.util.Try

/**
  * Talker is used to send messages to other peers over network.
  */
trait Talker {
  /**
    * Sends message of given body to specified recipient.
    *
    * @param recipient   recipient of the message
    * @param messageBody contents of the message
    * @return result of the operation (Success/Failure)
    */
  def send(recipient: NetworkPeer, messageBody: Serializable): Try[Unit]


  /**
    * Sends message of given body to specified recipient asynchronously.
    * The callback is executed and operation result is passed as argument.
    *
    * @param recipient   recipient of the message
    * @param messageBody contents of the message
    * @param callback    function executed after the operation is executed
    */
  def send(recipient: NetworkPeer, messageBody: Serializable, callback: Try[Unit] => Unit)

  /**
    * Shutdown all connections with peers
    */
  def shutdown()
}
