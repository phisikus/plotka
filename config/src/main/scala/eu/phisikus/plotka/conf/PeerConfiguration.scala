package eu.phisikus.plotka.conf

import scala.beans.BeanProperty

trait PeerConfiguration {
  @BeanProperty def address: String

  @BeanProperty def port: Int
}
