package eu.phisikus.plotka.network.talker

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels._
import java.util.concurrent.Executors

import com.twitter.chill.KryoInjection
import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.model.{NetworkMessage, NetworkPeer, Peer}

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap
import scala.util.Try

class NetworkTalker(localPeer: Peer) extends Talker {
  private val logger = Logger(classOf[NetworkTalker])
  private val peerChannelMap: TrieMap[NetworkPeer, SocketChannel] = new TrieMap()
  private val bufferWithZero: ByteBuffer = ByteBuffer.wrap(getIntAsBytes(0))
  private val maxNumberOfRetries = 10
  private val threadPool = Executors.newCachedThreadPool()

  override def send(recipient: NetworkPeer,
                    messageBody: Serializable,
                    callback: (Try[Unit]) => Unit): Unit = {
    threadPool.execute(() => callback(send(recipient, messageBody)))
  }

  override def send(recipient: NetworkPeer, messageBody: Serializable): Try[Unit] = {
    Try({
      val message = NetworkMessage(localPeer, recipient, messageBody)
      val messageAsBytes = getMessageAsBytes(message)
      val messageSizeAsBytes = getIntAsBytes(messageAsBytes.length)
      val messageBuffer = buildBufferForMessage(messageAsBytes, messageSizeAsBytes)

      sendWithRetry(recipient, messageBuffer, maxNumberOfRetries)
    })
  }

  override def shutdown(): Unit = {
    peerChannelMap.foreach(pair => {
      val channel = pair._2
      channel.write(bufferWithZero)
      channel.close()
    })
    peerChannelMap.clear()
  }

  private def getIntAsBytes(number: Int): Array[Byte] = {
    val intBuffer = ByteBuffer.allocate(4)
    intBuffer.putInt(number)
    intBuffer.array()
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
  private def sendWithRetry(recipient: NetworkPeer,
                            bufferWithMessage: ByteBuffer,
                            retryAttemptsLeft: Int): Unit = {
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

        if (retryAttemptsLeft > 0) {
          delayNextAttempt(retryAttemptsLeft)
          sendWithRetry(recipient, bufferWithMessage, retryAttemptsLeft - 1)
        } else {
          peerChannelMap.remove(recipient)
          throw exception
        }
    }
  }

  private def delayNextAttempt(retryAttemptsLeft: Int) = {
    val timeToSleep = Math.pow(2L, maxNumberOfRetries - retryAttemptsLeft).toLong
    Thread.sleep(timeToSleep)
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
    KryoInjection(testMessage)
  }

}
