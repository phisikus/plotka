package pl.weimaraner.plotka.conf.model

import pl.weimaraner.plotka.conf.PeerConfiguration

class BasicPeerConfiguration(val address: String, val port: Int = 3030) extends PeerConfiguration {

  def canEqual(other: Any): Boolean = other.isInstanceOf[BasicPeerConfiguration]

  override def equals(other: Any): Boolean = other match {
    case that: BasicPeerConfiguration =>
      (that canEqual this) &&
        address == that.address &&
        port == that.port
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(address, port)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }


  override def toString = s"BasicPeerConfiguration($address, $port)"
}
