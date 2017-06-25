package pl.weimaraner.plotka.network.handlers

import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model._

/**
  * This handler is called after read operation and performs deserialization of incoming message.
  * After that the message consumer is called followed by an attempt to order the next read operation.
  *
  * @param messageConsumer the message consumer that will be called after the message is received
  * @param messageBuffer buffer containing received incoming message
  * @param channel server channel
  */
class MessageContentHandler(messageConsumer: NetworkMessageConsumer,
                            channel: AsynchronousSocketChannel,
                            messageBuffer: ByteBuffer) extends CompletionHandler[Integer, SessionState] {
  private val logger = Logger(classOf[MessageContentHandler])

  override def completed(bytesRead: Integer, sessionState: SessionState): Unit = {
    messageConsumer.consumeMessage(readMessageFromBuffer(), sessionState)
    readNextMessageSize(sessionState)
  }

  private def readNextMessageSize(sessionState: SessionState) = {
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
    channel.read(messageSizeBuffer, sessionState, new MessageSizeHandler(messageConsumer, messageSizeBuffer, channel))
  }

  private def readMessageFromBuffer() = {
    messageBuffer.rewind()
    val byteInputStream = new ByteArrayInputStream(messageBuffer.array())
    val objectInputStream = new ObjectInputStream(byteInputStream)
    val inputObject = objectInputStream.readObject()
    val message = inputObject.asInstanceOf[Message[NetworkPeer, Peer, Serializable]]
    byteInputStream.close()
    objectInputStream.close()
    logger.debug(s"Received message from: ${channel.getRemoteAddress}, content: $message")
    message
  }

  override def failed(throwable: Throwable, state: SessionState): Unit = {
    logger.debug(s"Could not read message from:  ${channel.getRemoteAddress} caused by: $throwable")
  }


}
