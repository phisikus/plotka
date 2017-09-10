package eu.phisikus.plotka.conf.providers

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider, PeerConfiguration}

import scala.collection.JavaConverters._

/**
  * This implementation provides configuration loaded from file using Typesafe Config library.
  *
  * @param fileName optional name of the configuration file (resource)
  */
class FileConfigurationProvider(val fileName: Option[String]) extends NodeConfigurationProvider {

  private val logger = Logger(classOf[FileConfigurationProvider])

  def this() {
    this(None)
  }

  override def loadConfiguration: NodeConfiguration = {
    val loadedConfiguration = loadConfigurationFile
    val node = loadedConfiguration.getConfig("node")
    val nodeId = node.getString("id")
    val nodePort = node.getInt("port")
    val nodeAddress = node.getString("address")
    val peers = node.getConfigList("peers").asScala.toList
    BasicNodeConfiguration(nodeId, nodePort, nodeAddress, buildPeerConfigurations(peers))
  }

  private def loadConfigurationFile = {
    fileName match {
      case None =>
        logger.info(s"Loading default configuration.")
        ConfigFactory.load()
      case Some(name) =>
        logger.info(s"Loading configuration : $name")
        ConfigFactory.load(name)
    }
  }

  private def buildPeerConfigurations(peers: List[_ <: Config]): List[PeerConfiguration] = {
    val initialList = List[PeerConfiguration]()
    val foldingOperator = (config: Config, list: List[PeerConfiguration]) => buildPeerConfiguration(config) :: list
    peers.foldRight(initialList)(foldingOperator)
  }

  private def buildPeerConfiguration(peerConfig: Config): PeerConfiguration = {
    if (peerConfig.hasPath("port")) {
      BasicPeerConfiguration(peerConfig.getString("address"), peerConfig.getInt("port"))
    } else {
      BasicPeerConfiguration(peerConfig.getString("address"))
    }
  }


}
