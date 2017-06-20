package pl.weimaraner.plotka.network

import java.io.{IOException, ObjectInputStream}
import java.net.{InetAddress, ServerSocket, Socket}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.conf.NodeConfiguration
import pl.weimaraner.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer}

import scala.annotation.tailrec

class Listener(val nodeConfiguration: NodeConfiguration, val messageConsumer: NetworkMessageConsumer) {
  private val serverSocket = new ServerSocket(nodeConfiguration.port, 50, InetAddress.getByName(nodeConfiguration.address))
  private val threadPool = Executors.newFixedThreadPool(10)
  private val logger = Logger(classOf[Listener])

  def startServerLoop(): Unit = {
    threadPool.execute(() => runServerLoop())
  }

  @tailrec
  private def runServerLoop(): Unit = {
    logger.debug(s"Waiting for new connection (${nodeConfiguration.address}:${nodeConfiguration.port}) ... ")
    val newClientSocket = serverSocket.accept()
    logger.debug(s"Accepted connection: ${newClientSocket.getRemoteSocketAddress.toString}")
    threadPool.execute(handleClient(newClientSocket))
    runServerLoop()
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
