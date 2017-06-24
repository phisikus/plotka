package pl.weimaraner.plotka.network.handlers

import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model._

class MessageContentHandler(messageConsumer: NetworkMessageConsumer,
                            channel: AsynchronousSocketChannel,
                            messageBuffer: ByteBuffer) extends CompletionHandler[Integer, SessionState] {
  private val logger = Logger(classOf[MessageContentHandler])

  override def completed(bytesRead: Integer, sessionState: SessionState): Unit = {
    messageConsumer.consumeMessage(readMessageFromBuffer())
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
    channel.read(messageSizeBuffer, sessionState, new MessageSizeHandler(messageConsumer, channel, messageSizeBuffer))
  }

  private def readMessageFromBuffer() = {
    messageBuffer.rewind()
    val byteInputStream = new ByteArrayInputStream(messageBuffer.array())
    val objectInputStream = new ObjectInputStream(byteInputStream)
    val inputObject = objectInputStream.readObject()
    val message = inputObject.asInstanceOf[Message[NetworkPeer, NetworkPeer, Serializable]]
    byteInputStream.close()
    objectInputStream.close()
    logger.debug(s"Received message from: ${channel.getRemoteAddress}, content: $message")
    message
  }

  override def failed(throwable: Throwable, state: SessionState): Unit = {
    logger.debug(s"Could not read message from:  ${channel.getRemoteAddress} caused by: $throwable")
  }


}
