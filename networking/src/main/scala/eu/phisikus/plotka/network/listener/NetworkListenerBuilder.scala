package eu.phisikus.plotka.network.listener

import java.net.{InetAddress, UnknownHostException}
import java.util.UUID

import eu.phisikus.plotka.conf.PeerConfiguration
import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.model.NetworkMessageConsumer

import scala.annotation.tailrec

class NetworkListenerBuilder {
  private var id: String = UUID.randomUUID().toString
  private var port: Int = 3030
  private var address: String = getLocalAddress
  private var peers: List[PeerConfiguration] = List()
  private var messageConsumer: NetworkMessageConsumer = msg => {}

  def withId(newId: String): NetworkListenerBuilder = {
    id = newId
    this
  }

  def withPort(newPort: Int): NetworkListenerBuilder = {
    port = newPort
    this
  }

  def withAddress(newAddress: String): NetworkListenerBuilder = {
    address = newAddress
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
    peers = peerConfiguration :: peers
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
      BasicNodeConfiguration(id, port, address, peers),
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
