package eu.phisikus.plotka.conf

import scala.beans.BeanProperty

/**
  * It represents information required to connect to some peer in the network.
  * List of those objects would be used in configuration (NodeConfiguration).
  */
trait PeerConfiguration {
  @BeanProperty def address: String

  @BeanProperty def port: Int
}
