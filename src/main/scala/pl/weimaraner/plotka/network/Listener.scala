package pl.weimaraner.plotka.network

import java.net.InetSocketAddress
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousServerSocketChannel}
import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicBoolean

import com.typesafe.scalalogging.Logger
import pl.weimaraner.plotka.conf.NodeConfiguration
import pl.weimaraner.plotka.model.{NetworkMessageConsumer, SessionState}
import pl.weimaraner.plotka.network.handlers.AcceptHandler

class Listener(val nodeConfiguration: NodeConfiguration,
               val sessionStateConstructor: () => SessionState,
               val messageConsumer: NetworkMessageConsumer) {
  private val logger = Logger(classOf[Listener])
  private val serverThreadGroup = AsynchronousChannelGroup.withFixedThreadPool(10, Executors.defaultThreadFactory())
  private val serverSocketAddress = new InetSocketAddress(nodeConfiguration.address, nodeConfiguration.port)
  private var serverSocketChannel: AsynchronousServerSocketChannel = _


  def start(): Unit = {
    serverSocketChannel = AsynchronousServerSocketChannel.open(serverThreadGroup).bind(serverSocketAddress)
    val acceptHandler = new AcceptHandler(messageConsumer, sessionStateConstructor, serverSocketChannel)
    logger.info(s"Listener is waiting for connections: $serverSocketAddress")
    serverSocketChannel.accept(sessionStateConstructor.apply(), acceptHandler)
  }

  def stop(): Unit = {
    logger.info("Stopping the listener...")
    serverSocketChannel.close()
    logger.info("Communication channel and thread pool closed.")
  }

}
