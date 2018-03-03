package eu.phisikus.plotka.conf.mappers

import com.typesafe.config.Config
import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.conf.{NodeConfiguration, PeerConfiguration}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class ConfigToNodeConfigurationMapper {

  /**
    * Maps the data from instance of Config object to NodeConfiguration.
    *
    * @param config input configuration object
    * @return NodeConfiguration instance based on data from the input object
    */
  def map(config: Config): NodeConfiguration = {
    val node = config.getConfig("node")
    val nodeId = node.getString("id")
    val nodePort = node.getInt("port")
    val nodeAddress = node.getString("address")
    val peers = node.getConfigList("peers").asScala.toList
    val settings = getCustomSettings(node)
    BasicNodeConfiguration(nodeId, nodePort, nodeAddress, buildPeerConfigurations(peers), settings)
  }

  private def getCustomSettings(node: Config): Option[Config] = {
    Try {
      node.getConfig("settings")
    } match {
      case Success(settings) => Some(settings)
      case Failure(exception) => None
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
