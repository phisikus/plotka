package eu.phisikus.plotka.conf

import scala.beans.BeanProperty


trait NodeConfiguration {
  @BeanProperty def id: String

  @BeanProperty def port: Int

  @BeanProperty def address: String

  @BeanProperty def peers: List[PeerConfiguration]
}
