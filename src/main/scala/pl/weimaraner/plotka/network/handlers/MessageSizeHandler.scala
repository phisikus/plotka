package pl.weimaraner.plotka.network.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{NetworkMessageConsumer, SessionState}

class MessageSizeHandler(messageConsumer: NetworkMessageConsumer,
                         channel: AsynchronousSocketChannel,
                         messageSizeBuffer: ByteBuffer) extends CompletionHandler[Integer, SessionState] {
  private val logger = Logger(classOf[MessageSizeHandler])


  override def completed(bytesRead: Integer, sessionState: SessionState): Unit = {
    messageSizeBuffer.rewind()
    val messageSize = messageSizeBuffer.getInt
    if (Option(messageSize).isDefined) {
      logger.debug(s"Receiving message of size $messageSize from: ${channel.getRemoteAddress}")
      orderMessageRead(messageSize, sessionState)
    } else {
      logger.debug(s"The message size could not be determined for ${channel.getRemoteAddress}")
    }
  }

  def orderMessageRead(messageSize: Int, sessionState: SessionState): Unit = {
    val messageBuffer = ByteBuffer.allocate(messageSize)
    channel.read(messageBuffer, sessionState, new MessageContentHandler(messageConsumer, channel, messageBuffer))
  }


  override def failed(throwable: Throwable, state: SessionState): Unit = {
    logger.debug(s"Could not read message size from:  ${channel.getRemoteAddress} caused by: $throwable")
  }

}
