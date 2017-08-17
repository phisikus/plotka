package eu.phisikus.plotka.network.listener.handlers

import java.io.{ByteArrayInputStream, IOException, ObjectInputStream}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.model.{Message, NetworkMessageConsumer}
import eu.phisikus.plotka.model._

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

  private def processBufferData(state: Unit) = {
    readMessage(messageBuffer) match {
      case Some(message) => messageConsumer.consumeMessage(message)
      case None => logger.debug("Could not read message.")
    }

    readNextMessageSize(state)
  }

  private def readMessage(messageBuffer: ByteBuffer): Option[Message[NetworkPeer, Peer, Serializable]] = {
    try
      Some(readMessageFromBuffer())
    catch {
      case ioe: IOException =>
        logger.debug(s"Exception was thrown while reading message: $ioe")
        None
      case cnf: ClassNotFoundException =>
        logger.debug(s"Deserialization failed, class could not be found: $cnf")
        None
    }
  }

  private def readNextMessageSize(state: Unit) = {
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
    channel.read(messageSizeBuffer, state, new MessageSizeHandler(messageConsumer, messageSizeBuffer, channel))
  }

  private def readMessageFromBuffer(): Message[NetworkPeer, Peer, Serializable] = {
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

  override def failed(throwable: Throwable, state: Unit): Unit = {
    logger.debug(s"Could not read message from:  ${channel.getRemoteAddress} caused by: $throwable")
  }


}
