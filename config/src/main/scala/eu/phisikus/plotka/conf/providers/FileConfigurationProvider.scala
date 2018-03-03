package eu.phisikus.plotka.conf.providers

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.mappers.ConfigToNodeConfigurationMapper
import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider}

/**
  * This implementation provides configuration loaded from file using Typesafe Config library.
  *
  * @param fileName optional name of the configuration file (resource)
  */
class FileConfigurationProvider(val fileName: Option[String]) extends NodeConfigurationProvider {

  private val logger = Logger(classOf[FileConfigurationProvider])
  private val mappingStrategy = new ConfigToNodeConfigurationMapper

  def this() {
    this(None)
  }

  override def loadConfiguration: NodeConfiguration = {
    mappingStrategy.map(getConfigurationFromFactory)
  }

  private def getConfigurationFromFactory: Config = {
    fileName match {
      case None =>
        logger.info("Loading default configuration.")
        ConfigFactory.load()
      case Some(name) =>
        logger.info(s"Loading configuration : $name")
        ConfigFactory.load(name)
    }
  }


}
