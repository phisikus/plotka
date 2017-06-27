package pl.weimaraner.plotka.network.listener

import java.net.InetSocketAddress
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousServerSocketChannel}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.conf.NodeConfiguration
import pl.weimaraner.plotka.model.NetworkMessageConsumer
import pl.weimaraner.plotka.network.listener.handlers.AcceptHandler

/**
  * Listener creates a network socket and services incoming connections and messages.
  *
  * @param nodeConfiguration initial configuration
  * @param messageConsumer   handler that will be called for each received message
  */
class Listener(val nodeConfiguration: NodeConfiguration,
               val messageConsumer: NetworkMessageConsumer) {
  private val logger = Logger(classOf[Listener])
  private val serverThreadGroup = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory())
  private val serverSocketAddress = new InetSocketAddress(nodeConfiguration.address, nodeConfiguration.port)
  private var serverSocketChannel: AsynchronousServerSocketChannel = _


  def start(): Unit = {
    serverSocketChannel = AsynchronousServerSocketChannel.open(serverThreadGroup).bind(serverSocketAddress)
    val acceptHandler = new AcceptHandler(messageConsumer, serverSocketChannel)
    logger.info(s"Listener is waiting for connections: $serverSocketAddress")

    serverSocketChannel.accept((), acceptHandler)
  }

  def stop(): Unit = {
    logger.info("Stopping the listener...")
    serverSocketChannel.close()
    logger.info("Communication channel closed.")
  }

}
