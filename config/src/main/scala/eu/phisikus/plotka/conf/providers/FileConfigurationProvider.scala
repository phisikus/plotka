package eu.phisikus.plotka.conf.providers

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.conf.{NodeConfiguration, NodeConfigurationProvider, PeerConfiguration}

import scala.collection.JavaConverters._

class FileConfigurationProvider(val fileName: String = "application") extends NodeConfigurationProvider {

  private val logger = Logger(classOf[FileConfigurationProvider])

  override def loadConfiguration: NodeConfiguration = {
    logger.info(s"Loading configuration : $fileName")
    val loadedConfiguration = ConfigFactory.load(fileName)
    val node = loadedConfiguration.getConfig("node")
    val nodeId = node.getString("id")
    val nodePort = node.getInt("port")
    val nodeAddress = node.getString("address")
    val peers = node.getConfigList("peers").asScala.toList
    BasicNodeConfiguration(nodeId, nodePort, nodeAddress, buildPeerConfigurations(peers))
  }

  private def buildPeerConfigurations(peers: List[_ <: Config]): List[PeerConfiguration] = {
    peers match {
      case head :: tail => buildPeerConfiguration(head) :: buildPeerConfigurations(tail)
      case Nil => List()
    }
  }

  private def buildPeerConfiguration(peerConfig: Config): PeerConfiguration = {
    if (peerConfig.hasPath("port")) {
      BasicPeerConfiguration(peerConfig.getString("address"), peerConfig.getInt("port"))
    } else {
      BasicPeerConfiguration(peerConfig.getString("address"))
    }
  }


}
