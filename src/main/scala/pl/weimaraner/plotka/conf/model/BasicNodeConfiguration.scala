package pl.weimaraner.plotka.conf.model

import java.util.UUID

import pl.weimaraner.plotka.conf.{NodeConfiguration, PeerConfiguration}

case class BasicNodeConfiguration(id: String = UUID.randomUUID().toString,
                                  port: Int = 3030,
                                  address: String = "0.0.0.0",
                                  peers: List[PeerConfiguration]) extends NodeConfiguration

