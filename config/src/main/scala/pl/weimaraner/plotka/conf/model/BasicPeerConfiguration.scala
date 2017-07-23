package pl.weimaraner.plotka.conf.model

import pl.weimaraner.plotka.conf.PeerConfiguration

case class BasicPeerConfiguration(address: String, port: Int = 3030) extends PeerConfiguration