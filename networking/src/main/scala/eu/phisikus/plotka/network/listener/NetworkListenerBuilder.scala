package eu.phisikus.plotka.network.listener

import java.net.{InetAddress, UnknownHostException}
import java.util.UUID

import eu.phisikus.plotka.conf.PeerConfiguration
import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.model.NetworkMessageConsumer

import scala.annotation.tailrec

class NetworkListenerBuilder {
  protected var nodeConfiguration: BasicNodeConfiguration = BasicNodeConfiguration(
    UUID.randomUUID().toString,
    3030,
    getLocalAddress,
    Nil
  )
  protected var messageConsumer: NetworkMessageConsumer = msg => {}

  def withId(newId: String): NetworkListenerBuilder = {
    nodeConfiguration = nodeConfiguration.copy(id = newId)
    this
  }

  def withPort(newPort: Int): NetworkListenerBuilder = {
    nodeConfiguration = nodeConfiguration.copy(port = newPort)
    this
  }

  def withAddress(newAddress: String): NetworkListenerBuilder = {
    nodeConfiguration = nodeConfiguration.copy(address = newAddress)
    this
  }

  def withMessageHandler(newMessageConsumer: NetworkMessageConsumer): NetworkListenerBuilder = {
    messageConsumer = newMessageConsumer
    this
  }

  def withPeer(peerAddress: String, peerPort: Int = 3030): NetworkListenerBuilder = {
    withPeer(BasicPeerConfiguration(peerAddress, peerPort))
  }

  def withPeer(peerConfiguration: PeerConfiguration): NetworkListenerBuilder = {
    nodeConfiguration = nodeConfiguration.copy(peers = peerConfiguration :: nodeConfiguration.getPeers)
    this
  }

  @tailrec
  final def withPeers(peers: List[PeerConfiguration]): NetworkListenerBuilder = {
    peers match {
      case head :: tail => withPeer(head).withPeers(tail)
      case Nil => this
    }
  }

  def build(): NetworkListener = {
    new NetworkListener(
      nodeConfiguration,
      messageConsumer
    )
  }

  private def getLocalAddress: String = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: UnknownHostException => "127.0.0.1"
    }
  }

}

object NetworkListenerBuilder {
  def apply(): NetworkListenerBuilder = new NetworkListenerBuilder
}
