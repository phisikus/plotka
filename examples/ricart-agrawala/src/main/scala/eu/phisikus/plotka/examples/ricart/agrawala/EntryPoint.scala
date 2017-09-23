package eu.phisikus.plotka.examples.ricart.agrawala

import java.util.concurrent.ForkJoinPool

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider


object EntryPoint {
  private val logger = Logger("EntryPoint")
  private val configuration1Provider = new FileConfigurationProvider(Some("node1/application"))
  private val configuration2Provider = new FileConfigurationProvider(Some("node2/application"))
  private val node1Conf = configuration1Provider.loadConfiguration
  private val node2Conf = configuration2Provider.loadConfiguration

  def main(args: Array[String]): Unit = {
    val node1 = new RicartAgrawalaNode(node1Conf, () => {
      logger.info("HELLO ")
    })

    val node2 = new RicartAgrawalaNode(node2Conf, () => {
      logger.info("WORLD!")
    })

    val executorService = ForkJoinPool.commonPool()
    executorService.execute(() => node1.start())
    executorService.execute(() => node2.start())
    System.in.read()
    node1.stop()
    node2.stop()
  }
}
