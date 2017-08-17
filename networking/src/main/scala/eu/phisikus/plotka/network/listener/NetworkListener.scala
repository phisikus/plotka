package eu.phisikus.plotka.network.listener

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousServerSocketChannel}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.NodeConfiguration
import eu.phisikus.plotka.model.NetworkMessageConsumer
import eu.phisikus.plotka.network.listener.handlers.AcceptHandler

/**
  * NetworkListener creates a network socket and services incoming connections and messages.
  *
  * @param nodeConfiguration initial configuration
  * @param messageConsumer   handler that will be called for each received message
  */
class NetworkListener(val nodeConfiguration: NodeConfiguration,
                      val messageConsumer: NetworkMessageConsumer) {
  private val logger = Logger(classOf[NetworkListener])
  private val serverThreadGroup = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory())
  private val serverSocketAddress = new InetSocketAddress(nodeConfiguration.address, nodeConfiguration.port)
  private var serverSocketChannel: AsynchronousServerSocketChannel = _

  def start(): Unit = {
    serverSocketChannel = AsynchronousServerSocketChannel.open(serverThreadGroup).bind(serverSocketAddress)
    val acceptHandler = new AcceptHandler(messageConsumer, serverSocketChannel)
    logger.info(s"NetworkListener is waiting for connections: $serverSocketAddress")

    serverSocketChannel.accept((), acceptHandler)
  }

  def stop(): Unit = {
    logger.info("Stopping the listener...")
    try {
      serverSocketChannel.close()
      serverThreadGroup.shutdownNow()
    }
    catch {
      case e: IOException => logger.debug(s"Exception was thrown during stop(): $e")
    }
    logger.info("Communication channel closed.")
  }

}
