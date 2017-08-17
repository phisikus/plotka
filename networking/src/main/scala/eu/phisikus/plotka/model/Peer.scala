package eu.phisikus.plotka.model

import java.util.UUID

sealed class Peer(id: String) extends Serializable

case class NetworkPeer(id: String, address: String, port: Int) extends Peer(id = id) {
  def this(address: String, port: Int) = {
    this(UUID.randomUUID().toString, address, port)
  }
}
