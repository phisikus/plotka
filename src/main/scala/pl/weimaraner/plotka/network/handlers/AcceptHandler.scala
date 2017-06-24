package pl.weimaraner.plotka.network.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{NetworkMessageConsumer, SessionState}

class AcceptHandler(messageConsumer: NetworkMessageConsumer,
                    sessionStateConstructor: () => SessionState,
                    serverSocketChannel: AsynchronousServerSocketChannel) extends CompletionHandler[AsynchronousSocketChannel, SessionState] {
  private val logger = Logger(classOf[AcceptHandler])
  private val IntegerSize = 4

  override def completed(channel: AsynchronousSocketChannel, sessionState: SessionState): Unit = {
    logger.debug(s"Accepted connection: ${channel.getRemoteAddress.toString}")
    acceptNextConnection()
    orderReadMessageSize(channel, sessionState)
  }

  private def acceptNextConnection() = {
    serverSocketChannel.accept(sessionStateConstructor.apply(), this)
  }

  private def orderReadMessageSize(channel: AsynchronousSocketChannel, sessionState: SessionState) = {
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(IntegerSize)
    channel.read(messageSizeBuffer, sessionState, new MessageSizeHandler(messageConsumer, channel, messageSizeBuffer))
  }

  override def failed(throwable: Throwable, a: SessionState): Unit = {
    logger.debug(s"Accept operation failed: $throwable")
  }
}
