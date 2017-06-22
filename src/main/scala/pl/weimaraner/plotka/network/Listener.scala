package pl.weimaraner.plotka.network

import java.io.{IOException, ObjectInputStream}
import java.net.{InetSocketAddress, Socket}
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousServerSocketChannel}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.conf.NodeConfiguration
import pl.weimaraner.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer}

import scala.annotation.tailrec

class Listener(val nodeConfiguration: NodeConfiguration, val messageConsumer: NetworkMessageConsumer) {
  private val logger = Logger(classOf[Listener])
  private val serverThreadGroup = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory())
  private val serverSocketAddress = new InetSocketAddress(nodeConfiguration.address, nodeConfiguration.port)
  private val serverSocketChannel = AsynchronousServerSocketChannel.open(serverThreadGroup).bind(serverSocketAddress)
  serverSocketChannel.accept()

  //https://chamibuddhika.wordpress.com/2012/08/11/io-demystified/
  def start(): Unit = {

  }

  private def handleClient(clientSocket: Socket): Runnable = {
    () => {
      val clientAddress = clientSocket.getRemoteSocketAddress.toString
      try {
        val dataStream = new ObjectInputStream(clientSocket.getInputStream)
        consumeMessages(dataStream, clientAddress)
      } catch {
        case e: IOException =>
      } finally {
        logger.debug(s"Closing client socket: $clientAddress")
        clientSocket.close()
      }
    }
  }

  @tailrec
  private def consumeMessages(dataStream: ObjectInputStream, clientAddress: String): Unit = {
    val incommingMessage = receiveMessage(dataStream)
    logger.debug(s"Received message from: $clientAddress")
    messageConsumer.consumeMessage(incommingMessage)
    consumeMessages(dataStream, clientAddress)
  }

  private def receiveMessage(dataStream: ObjectInputStream): Message[NetworkPeer, NetworkPeer, Serializable] = {
    val incommingObject = dataStream.readObject()
    incommingObject.asInstanceOf[Message[NetworkPeer, NetworkPeer, Serializable]]
  }

}
