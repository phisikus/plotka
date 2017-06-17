package pl.weimaraner.plotka.model

sealed class Peer(id: String)

case class NetworkPeer(id: String, address: String, port: Int) extends Peer(id = id)
