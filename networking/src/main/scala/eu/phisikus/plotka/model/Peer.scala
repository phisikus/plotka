package eu.phisikus.plotka.model

import java.util.UUID

import scala.beans.BeanProperty

sealed class Peer(@BeanProperty id: String) extends Serializable

case class NetworkPeer(@BeanProperty id: String,
                       @BeanProperty address: String,
                       @BeanProperty port: Int) extends Peer(id = id) {
  def this(address: String, port: Int) = {
    this(UUID.randomUUID().toString, address, port)
  }
}
