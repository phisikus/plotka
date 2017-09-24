package eu.phisikus.plotka.network.listener.handlers

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.model.NetworkMessageConsumer

import scala.util.{Success, Try}

/**
  * This handler is called when the size of the message is received.
  * It triggers a read operation for the incoming message.
  *
  * @param messageConsumer   the message consumer that will be called after the message is received
  * @param messageSizeBuffer buffer containing received incoming message size
  * @param channel           server channel
  */
class MessageSizeHandler(messageConsumer: NetworkMessageConsumer,
                         messageSizeBuffer: ByteBuffer,
                         channel: AsynchronousSocketChannel) extends CompletionHandler[Integer, Unit] {
  private val logger = Logger(classOf[MessageSizeHandler])


  override def completed(bytesRead: Integer, state: Unit): Unit = {
    messageSizeBuffer.rewind()
    val messageSize = messageSizeBuffer.getInt
    Option(messageSize) match {
      case Some(x) if x > 0 => orderMessageRead(messageSize, state)
      case Some(x) if x < 0 => logger.debug(s"The message size is incorrect! (${channel.getRemoteAddress})")
      case Some(0) => closeTransmission()
      case None => logger.debug(s"The message size could not be determined for ${channel.getRemoteAddress}")
    }
  }

  private def closeTransmission(): Unit = {
    try {
      logger.debug(s"Peer declared end of transmission: ${channel.getRemoteAddress}")
      channel.close()
    }
    catch {
      case e: IOException => logger.debug(s"Exception thrown during closeTransmission(): $e")
    }
  }

  def orderMessageRead(messageSize: Int, state: Unit): Unit = {
    logger.debug(s"Receiving message of size $messageSize from: ${channel.getRemoteAddress}")
    val messageBuffer = ByteBuffer.allocate(messageSize)
    channel.read(messageBuffer, state, new MessageContentHandler(messageConsumer, channel, messageBuffer, messageSize))
  }


  override def failed(throwable: Throwable, state: Unit): Unit = {
    Try(channel.getRemoteAddress) match {
      case Success(address) => logger.debug(s"Could not read message size from:  $address caused by: $throwable")
      case _ => logger.debug(s"Could not read message size - caused by: $throwable")
    }

  }

}
