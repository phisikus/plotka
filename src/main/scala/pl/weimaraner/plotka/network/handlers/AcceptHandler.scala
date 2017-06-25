package pl.weimaraner.plotka.network.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousCloseException, AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{NetworkMessageConsumer, SessionState}

/**
  * This handler is called when new client connection is accepted.
  * It enables future accepts and orders a read operation that will return a single integer.
  * That value represents size in bytes of an incoming message object.
  * Every read operation is asynchronous so the result will available to the next handler
  *
  * @param messageConsumer the message consumer that will be called after the message is received
  * @param sessionStateConstructor factory method for the initial session state object
  * @param channel server channel
  */
class AcceptHandler(messageConsumer: NetworkMessageConsumer,
                    sessionStateConstructor: () => SessionState,
                    channel: AsynchronousServerSocketChannel) extends CompletionHandler[AsynchronousSocketChannel, SessionState] {
  private val logger = Logger(classOf[AcceptHandler])
  private val IntegerSize = 4

  override def completed(channel: AsynchronousSocketChannel, sessionState: SessionState): Unit = {
    logger.debug(s"Accepted connection: ${channel.getRemoteAddress.toString}")
    acceptNextConnection()
    orderReadMessageSize(channel, sessionState)
  }

  private def acceptNextConnection() = {
    channel.accept(sessionStateConstructor.apply(), this)
  }

  private def orderReadMessageSize(channel: AsynchronousSocketChannel, sessionState: SessionState) = {
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(IntegerSize)
    channel.read(messageSizeBuffer, sessionState, new MessageSizeHandler(messageConsumer, messageSizeBuffer, channel))
  }

  override def failed(throwable: Throwable, sessionState: SessionState): Unit = {
    throwable match {
      case _ : AsynchronousCloseException =>
      case e : Throwable => logger.debug(s"Accept operation failed: $e")
    }
  }
}
