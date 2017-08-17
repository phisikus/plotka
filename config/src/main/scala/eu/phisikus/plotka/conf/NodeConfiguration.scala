package eu.phisikus.plotka.conf

trait NodeConfiguration {
  def id: String

  def port: Int

  def address: String

  def peers: List[PeerConfiguration]
}
