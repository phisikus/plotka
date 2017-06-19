package pl.weimaraner.plotka.conf

trait NodeConfigurationProvider {
  def loadConfiguration: NodeConfiguration
}
