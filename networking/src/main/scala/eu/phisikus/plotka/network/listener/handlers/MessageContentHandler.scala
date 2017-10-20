package eu.phisikus.plotka.network.listener.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.twitter.chill.KryoInjection
import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.model.{Message, NetworkMessageConsumer, _}

import scala.util.{Failure, Success}

/**
  * This handler is called after read operation and performs deserialization of incoming message.
  * After that the message consumer is called followed by an attempt to order the next read operation.
  *
  * @param messageConsumer  the message consumer that will be called after the message is received
  * @param channel          server channel
  * @param messageBuffer    buffer containing received incoming message
  * @param expectedDataSize expected size of data in buffer
  */
class MessageContentHandler(messageConsumer: NetworkMessageConsumer,
                            channel: AsynchronousSocketChannel,
                            messageBuffer: ByteBuffer,
                            expectedDataSize: Int) extends CompletionHandler[Integer, Unit] {
  private val logger = Logger(classOf[MessageContentHandler])

  override def completed(bytesRead: Integer, state: Unit): Unit = {
    if (messageBuffer.position < expectedDataSize) {
      channel.read(messageBuffer, state, this)
    } else {
      processBufferData(state)
    }
  }

  private def processBufferData(state: Unit): Unit = {
    readMessage(messageBuffer) match {
      case Some(message) => messageConsumer.consumeMessage(message)
      case None => logger.debug("Could not read message.")
    }

    readNextMessageSize(state)
  }

  private def readMessage(messageBuffer: ByteBuffer): Option[Message[NetworkPeer, Peer, Serializable]] = {
    messageBuffer.rewind()
    val deserialization = KryoInjection.invert(messageBuffer.array())
    deserialization match {
      case Success(message) =>
        logger.debug(s"Received message from: ${channel.getRemoteAddress}, content: $message")
        Some(message.asInstanceOf[Message[NetworkPeer, Peer, Serializable]])
      case Failure(error) =>
        logger.debug(s"Failed to deserialize message from : ${channel.getRemoteAddress}, reason: $error")
        None
    }
  }

  private def readNextMessageSize(state: Unit): Unit = {
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
    channel.read(messageSizeBuffer, state, new MessageSizeHandler(messageConsumer, messageSizeBuffer, channel))
  }

  override def failed(throwable: Throwable, state: Unit): Unit = {
    logger.debug(s"Could not read message from:  ${channel.getRemoteAddress} caused by: $throwable")
  }


}
