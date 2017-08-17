package eu.phisikus.plotka.network.listener.handlers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousCloseException, AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.model.NetworkMessageConsumer

/**
  * This handler is called when new client connection is accepted.
  * It enables future accepts and orders a read operation that will return a single integer.
  * That value represents size in bytes of an incoming message object.
  * Every read operation is asynchronous so the result will available to the next handler
  *
  * @param messageConsumer the message consumer that will be called after the message is received
  * @param channel         server channel
  */
class AcceptHandler(messageConsumer: NetworkMessageConsumer,
                    channel: AsynchronousServerSocketChannel) extends CompletionHandler[AsynchronousSocketChannel, Unit] {
  private val logger = Logger(classOf[AcceptHandler])
  private val IntegerSize = 4

  override def completed(channel: AsynchronousSocketChannel, state: Unit): Unit = {
    logger.debug(s"Accepted connection: ${channel.getRemoteAddress.toString}")
    acceptNextConnection()
    orderReadMessageSize(channel, state)
  }

  private def acceptNextConnection() = {
    channel.accept((), this)
  }

  private def orderReadMessageSize(channel: AsynchronousSocketChannel, state: Unit) = {
    val messageSizeBuffer: ByteBuffer = ByteBuffer.allocate(IntegerSize)
    channel.read(messageSizeBuffer, state, new MessageSizeHandler(messageConsumer, messageSizeBuffer, channel))
  }

  override def failed(throwable: Throwable, state: Unit): Unit = {
    throwable match {
      case _: AsynchronousCloseException =>
      case e: Throwable => logger.debug(s"Accept operation failed: $e")
    }
  }
}
