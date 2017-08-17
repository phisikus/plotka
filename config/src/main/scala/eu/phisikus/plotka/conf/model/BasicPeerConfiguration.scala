package eu.phisikus.plotka.conf.model

import eu.phisikus.plotka.conf.PeerConfiguration

case class BasicPeerConfiguration(address: String, port: Int = 3030) extends PeerConfiguration

