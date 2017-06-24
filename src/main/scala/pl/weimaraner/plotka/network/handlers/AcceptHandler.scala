package pl.weimaraner.plotka.network.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{NetworkMessageConsumer, SessionState}

class AcceptHandler(messageConsumer: NetworkMessageConsumer) extends CompletionHandler[AsynchronousSocketChannel, SessionState] {
  private val logger = Logger(classOf[AcceptHandler])

  override def completed(channel: AsynchronousSocketChannel, sessionState: SessionState): Unit = {
    logger.debug(s"Accepted connection: ${channel.getRemoteAddress.toString}")
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
    channel.read(messageSizeBuffer, sessionState, new MessageSizeHandler(messageConsumer, channel, messageSizeBuffer))
  }

  override def failed(throwable: Throwable, a: SessionState): Unit = {
    logger.debug(s"Accept operation failed: $throwable")
  }
}
