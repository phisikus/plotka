package pl.weimaraner.plotka.network.talker

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.net.{InetSocketAddress, StandardSocketOptions}
import java.nio.ByteBuffer
import java.nio.channels._

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.model.{NetworkMessage, NetworkPeer, Peer}

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap

class NetworkTalker(localPeer: Peer) extends Talker {
  private val logger = Logger(classOf[NetworkTalker])
  private val peerChannelMap: TrieMap[NetworkPeer, SocketChannel] = new TrieMap()
  private val bufferWithZero: ByteBuffer = ByteBuffer.wrap(getIntAsBytes(0))

  override def send(recipient: NetworkPeer, messageBody: Serializable): Unit = {
    val message = NetworkMessage(localPeer, recipient, messageBody)
    val messageAsBytes = getMessageAsBytes(message)
    val messageSizeAsBytes = getIntAsBytes(messageAsBytes.length)
    val messageBuffer = buildBufferForMessage(messageAsBytes, messageSizeAsBytes)

    sendWithRetry(recipient, messageBuffer)
  }

  private def getIntAsBytes(number: Int): Array[Byte] = {
    val intBuffer = ByteBuffer.allocate(4)
    intBuffer.putInt(number)
    intBuffer.array()
  }

  override def shutdown(): Unit = {
    peerChannelMap.foreach(pair => {
      val channel = pair._2
      channel.write(bufferWithZero)
      channel.close()
    })
    peerChannelMap.clear()
  }

  private def buildBufferForMessage(messageAsBytes: Array[Byte], messageSizeAsBytes: Array[Byte]) = {
    val buffer = ByteBuffer
      .allocate(messageSizeAsBytes.length + messageAsBytes.length)
      .put(messageSizeAsBytes)
      .put(messageAsBytes)
    buffer.rewind()
    buffer
  }

  @tailrec
  private def sendWithRetry(recipient: NetworkPeer, bufferWithMessage: ByteBuffer): Unit = {
    val clientChannel: SocketChannel = getOpenChannelForPeer(recipient)
    logger.debug(s"Sending data to: ${clientChannel.getRemoteAddress}")
    try {
      while (bufferWithMessage.hasRemaining) {
        clientChannel.write(bufferWithMessage)
      }
    }
    catch {
      case exception: Exception =>
        logger.debug(s"Failed to send message, retrying. Reason: $exception")
        bufferWithMessage.rewind()
        sendWithRetry(recipient, bufferWithMessage)
    }

  }

  private def getOpenChannelForPeer(recipient: NetworkPeer): SocketChannel = {
    val retrievedChannel: SocketChannel = getChannelForPeer(recipient)

    if (waitForConnection(retrievedChannel)) {
      retrievedChannel
    } else {
      if (isChannelConnected(retrievedChannel)) {
        retrievedChannel
      } else {
        val newChannel = buildChannelForPeer(recipient)
        peerChannelMap.put(recipient, newChannel)
        newChannel
      }
    }

  }

  private def isChannelConnected(retrievedChannel: SocketChannel) = {
    retrievedChannel.isOpen && retrievedChannel.isConnected
  }

  private def waitForConnection(retrievedChannel: SocketChannel) = {
    retrievedChannel.isConnectionPending && retrievedChannel.finishConnect()
  }

  private def getChannelForPeer(recipient: NetworkPeer): SocketChannel = {
    peerChannelMap.getOrElseUpdate(recipient, buildChannelForPeer(recipient))
  }

  private def buildChannelForPeer(recipient: NetworkPeer): SocketChannel = {
    val hostAddress = new InetSocketAddress(recipient.address, recipient.port)
    val clientChannel = SocketChannel.open(hostAddress)
    clientChannel
  }

  private def getMessageAsBytes(testMessage: NetworkMessage): Array[Byte] = {
    val byteOutputStream = new ByteArrayOutputStream()
    val objectStream = new ObjectOutputStream(byteOutputStream)
    objectStream.writeObject(testMessage)
    objectStream.flush()
    objectStream.close()
    byteOutputStream.close()
    byteOutputStream.toByteArray
  }

}
