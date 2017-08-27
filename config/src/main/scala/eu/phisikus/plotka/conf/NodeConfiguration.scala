package eu.phisikus.plotka.conf

import scala.beans.BeanProperty


/**
  * It represents node configuration describing network settings and initial list of other peers.
  */
trait NodeConfiguration {
  @BeanProperty def id: String

  @BeanProperty def port: Int

  @BeanProperty def address: String

  @BeanProperty def peers: List[PeerConfiguration]
}
