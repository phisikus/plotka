package eu.phisikus.plotka.conf.model

import java.util.UUID

import eu.phisikus.plotka.conf.{NodeConfiguration, PeerConfiguration}

case class BasicNodeConfiguration(id: String = UUID.randomUUID().toString,
                                  port: Int = 3030,
                                  address: String = "0.0.0.0",
                                  peers: List[PeerConfiguration]) extends NodeConfiguration

