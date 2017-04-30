package pl.weimaraner.plotka.conf.model

import java.util.UUID

import pl.weimaraner.plotka.conf.{NodeConfiguration, PeerConfiguration}

class BasicNodeConfiguration(val id: String = UUID.randomUUID().toString,
                             val port: Int = 3030,
                             val address: String = "0.0.0.0",
                             val peers: List[PeerConfiguration]) extends NodeConfiguration {

  def canEqual(other: Any): Boolean = other.isInstanceOf[BasicNodeConfiguration]

  override def equals(other: Any): Boolean = other match {
    case that: BasicNodeConfiguration =>
      (that canEqual this) &&
        id == that.id &&
        port == that.port &&
        address == that.address &&
        peers == that.peers
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(id, port, address, peers)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }


  override def toString = s"BasicNodeConfiguration($id, $port, $address, $peers)"
}

