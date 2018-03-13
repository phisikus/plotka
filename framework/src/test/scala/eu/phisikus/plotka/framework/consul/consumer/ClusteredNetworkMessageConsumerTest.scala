package eu.phisikus.plotka.framework.consul.consumer

import java.util.UUID

import eu.phisikus.plotka.framework.consul.ConsulServiceRegistryManager
import eu.phisikus.plotka.model.{Message, NetworkMessage, NetworkPeer, Peer}
import org.mockito.Mockito.verify
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar


class ClusteredNetworkMessageConsumerTest extends FunSuite with MockitoSugar {

  test("Should create functional clustered network message consumer") {
    val localPeer = NetworkPeer(UUID.randomUUID().toString, "127.0.0.1", 4040)
    val registryManager = mock[ConsulServiceRegistryManager]
    val testMessage = mock[NetworkMessage]

    val messageConsumer = new ClusteredNetworkMessageConsumer(localPeer, registryManager,
      (message, talker, clusterList) => {
        message.getMessage()
        clusterList.apply()
      })

    messageConsumer.consumeMessage(testMessage.asInstanceOf[Message[NetworkPeer, Peer, Serializable]])

    verify(registryManager).getPeers()
    verify(testMessage).getMessage()

  }

}
