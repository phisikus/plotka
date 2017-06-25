package pl.weimaraner.plotka.network.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{NetworkMessageConsumer, SessionState}

/**
  * This handler is called when the size of the message is received.
  * It triggers a read operation for the incoming message.
  *
  * @param messageConsumer the message consumer that will be called after the message is received
  * @param messageSizeBuffer buffer containing received incoming message size
  * @param channel server channel
  */
class MessageSizeHandler(messageConsumer: NetworkMessageConsumer,
                         messageSizeBuffer: ByteBuffer,
                         channel: AsynchronousSocketChannel) extends CompletionHandler[Integer, SessionState] {
  private val logger = Logger(classOf[MessageSizeHandler])


  override def completed(bytesRead: Integer, sessionState: SessionState): Unit = {
    messageSizeBuffer.rewind()
    val messageSize = messageSizeBuffer.getInt
    Option(messageSize) match {
      case Some(x) if x > 0 => orderMessageRead(messageSize, sessionState)
      case Some(0) => closeTransmission()
      case None => logger.debug(s"The message size could not be determined for ${channel.getRemoteAddress}")
    }
  }

  private def closeTransmission() = {
    logger.debug(s"Peer declared end of transmission: ${channel.getRemoteAddress}")
    channel.close()
  }

  def orderMessageRead(messageSize: Int, sessionState: SessionState): Unit = {
    logger.debug(s"Receiving message of size $messageSize from: ${channel.getRemoteAddress}")
    val messageBuffer = ByteBuffer.allocate(messageSize)
    channel.read(messageBuffer, sessionState, new MessageContentHandler(messageConsumer, channel, messageBuffer))
  }


  override def failed(throwable: Throwable, state: SessionState): Unit = {
    logger.debug(s"Could not read message size from:  ${channel.getRemoteAddress} caused by: $throwable")
  }

}
