package eu.phisikus.plotka.conf

trait NodeConfigurationProvider {
  def loadConfiguration: NodeConfiguration
}
