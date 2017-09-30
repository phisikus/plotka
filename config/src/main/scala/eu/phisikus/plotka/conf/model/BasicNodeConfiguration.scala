package eu.phisikus.plotka.conf.model

import java.util.UUID

import com.typesafe.config.Config
import eu.phisikus.plotka.conf.{NodeConfiguration, PeerConfiguration}

import scala.beans.BeanProperty

case class BasicNodeConfiguration(@BeanProperty id: String = UUID.randomUUID().toString,
                                  @BeanProperty port: Int = 3030,
                                  @BeanProperty address: String = "0.0.0.0",
                                  @BeanProperty peers: List[PeerConfiguration],
                                  @BeanProperty settings: Option[Config] = None) extends NodeConfiguration
