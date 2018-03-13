package eu.phisikus.plotka.framework.consul

import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.framework.consul.consumer.ClusterPeerListProvider
import eu.phisikus.plotka.model._
import eu.phisikus.plotka.network.talker.Talker
import org.mockito.Matchers.any
import org.mockito.Mockito.verify
import org.mockito.internal.verification.Times
import org.scalatest.mockito.MockitoSugar

import scala.io.Source

class ClusteredNetworkListenerBuilderTest extends AbstractConsulTest with MockitoSugar {

  lazy val testCustomConfigurationText: String = Source
    .fromResource("clustered_node.conf")
    .getLines
    .map(line => line + "\n")
    .mkString
  val expectedConfiguration = BasicNodeConfiguration("node0.network", 3050, "127.0.0.1", Nil)

  test("Should build network listener that loads configuration from consul") {
    consulKVClient.putValue("configuration", testCustomConfigurationText)
    val listener = ClusteredNetworkListenerBuilder()
      .withServiceName("testService")
      .withConsulUrl(consulUrl)
      .withConsulNodeConfiguration("configuration")
      .build()

    listener.nodeConfiguration should equal(expectedConfiguration)
  }

  test("Should build network listener with advanced message handler") {
    val advancedHandler = mock[(NetworkMessage, Talker, ClusterPeerListProvider) => Unit]
    val networkMessage = mock[NetworkMessage]
    val listener = ClusteredNetworkListenerBuilder()
      .withConsulUrl(consulUrl)
      .withAdvancedMessageHandler(advancedHandler)
      .build()

    listener.messageConsumer.consumeMessage(networkMessage.asInstanceOf[Message[NetworkPeer, Peer, Serializable]])
    verify(advancedHandler, new Times(1)).apply(any[NetworkMessage], any[Talker], any[ClusterPeerListProvider])
  }

}
