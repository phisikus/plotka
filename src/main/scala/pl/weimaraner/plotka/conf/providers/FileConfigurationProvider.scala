package pl.weimaraner.plotka.conf.providers

import com.typesafe.config.{Config, ConfigFactory}
import pl.weimaraner.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import pl.weimaraner.plotka.conf.{NodeConfiguration, NodeConfigurationProvider, PeerConfiguration}

import scala.collection.JavaConverters._

class FileConfigurationProvider(val fileName: String = "application") extends NodeConfigurationProvider {

  override def loadConfiguration: NodeConfiguration = {
    val loadedConfiguration = ConfigFactory.load(fileName)
    val node = loadedConfiguration.getConfig("node")
    val nodeId = node.getString("id")
    val nodePort = node.getInt("port")
    val nodeAddress = node.getString("address")
    val peers = node.getConfigList("peers").asScala.toList
    new BasicNodeConfiguration(nodeId, nodePort, nodeAddress, buildPeerConfigurations(peers))
  }

  private def buildPeerConfigurations(peers: List[_ <: Config]): List[PeerConfiguration] = {
    peers match {
      case head :: tail => buildPeerConfiguration(head) :: buildPeerConfigurations(tail)
      case Nil => List()
    }
  }

  private def buildPeerConfiguration(peerConfig: Config): PeerConfiguration = {
    if (peerConfig.hasPath("port")) {
      new BasicPeerConfiguration(peerConfig.getString("address"), peerConfig.getInt("port"))
    } else {
      new BasicPeerConfiguration(peerConfig.getString("address"))
    }
  }


}
