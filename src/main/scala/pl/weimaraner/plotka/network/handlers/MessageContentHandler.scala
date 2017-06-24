package pl.weimaraner.plotka.network.handlers

import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer, SessionState}

class MessageContentHandler(messageConsumer: NetworkMessageConsumer,
                            channel: AsynchronousSocketChannel,
                            messageBuffer: ByteBuffer) extends CompletionHandler[Integer, SessionState] {
  private val logger = Logger(classOf[MessageContentHandler])

  override def completed(bytesRead: Integer, a: SessionState): Unit = {
    messageBuffer.rewind()
    val byteInputStream = new ByteArrayInputStream(messageBuffer.array())
    val objectInputStream = new ObjectInputStream(byteInputStream)
    val inputObject = objectInputStream.readObject()
    val message = inputObject.asInstanceOf[Message[NetworkPeer, NetworkPeer, Serializable]]
    messageConsumer.consumeMessage(message)
  }

  override def failed(throwable: Throwable, state: SessionState): Unit = {
    logger.debug(s"Could not read message from:  ${channel.getRemoteAddress} caused by: $throwable")
  }


}
