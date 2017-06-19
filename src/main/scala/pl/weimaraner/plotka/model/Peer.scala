package pl.weimaraner.plotka.model

sealed class Peer(id: String) extends Serializable

case class NetworkPeer(id: String, address: String, port: Int) extends Peer(id = id)
