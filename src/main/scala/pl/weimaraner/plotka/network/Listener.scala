package pl.weimaraner.plotka.network

import java.io.ObjectInputStream
import java.net.{InetAddress, ServerSocket, Socket}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.conf.NodeConfiguration
import pl.weimaraner.plotka.model.{Message, NetworkMessage, NetworkMessageConsumer, NetworkPeer}

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
    threadPool.execute(handleClient(newClientSocket))
    runServerLoop()
  }

  private def handleClient(clientSocket: Socket): Runnable = {
    () => {
      val incommingMessage = receiveMessage(clientSocket)
      logger.debug(s"Received message from: ${clientSocket.getRemoteSocketAddress.toString}")
      messageConsumer.consumeMessage(incommingMessage)
    }
  }

  private def receiveMessage(clientSocket: Socket): Message[NetworkPeer, NetworkPeer, Serializable] = {
    val dataFromClient = new ObjectInputStream(clientSocket.getInputStream)
    val incommingObject = dataFromClient.readObject()
    incommingObject.asInstanceOf[Message[NetworkPeer, NetworkPeer, Serializable]]
  }

}
