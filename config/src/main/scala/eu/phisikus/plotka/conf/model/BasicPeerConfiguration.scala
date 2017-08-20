package eu.phisikus.plotka.conf.model

import eu.phisikus.plotka.conf.PeerConfiguration

import scala.beans.BeanProperty

case class BasicPeerConfiguration(
                                   @BeanProperty address: String,
                                   @BeanProperty port: Int = 3030) extends PeerConfiguration

