package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.{AgentClient, Consul}
import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import eu.phisikus.plotka.framework.consul.ConsulServiceMapMatcher.containsService
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.listener.NetworkListener
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, Matchers}

class ClusteredNetworkListenerTest extends FunSuite
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfter
  with Eventually {

  private val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()
  private val serviceName = "testService"
  private val consulUrl = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort
  private val consulAgentClient: AgentClient = Consul
    .builder()
    .withUrl(consulUrl)
    .build()
    .agentClient()

  before {
    testConsul.reset()
  }

  override protected def afterAll(): Unit = {
    testConsul.close()
  }

  test("Should register service on start") {
    val testListener = ClusteredNetworkListenerBuilder()
      .withServiceName(serviceName)
      .withConsulUrl(consulUrl)
      .withPort(3040)
      .build()

    testListener.start()
    consulAgentClient.getServices should containsService(serviceName, testListener.nodeConfiguration)
    testListener.stop()
  }

  test("Should unregister service on stop") {
    val testListener = ClusteredNetworkListenerBuilder()
      .withServiceName(serviceName)
      .withConsulUrl(consulUrl)
      .withPort(3041)
      .build()

    testListener.start()
    testListener.stop()
    consulAgentClient.getServices shouldNot containsService(serviceName, testListener.nodeConfiguration)

  }

  test("Should register two services and provide peer information") {
    val firstListener: ClusteredNetworkListener = ClusteredNetworkListenerBuilder()
      .withConsulUrl(consulUrl)
      .withPort(3042)
      .build()

    val secondListener = ClusteredNetworkListenerBuilder()
      .withPort(3044)
      .withConsulUrl(consulUrl)
      .build()


    firstListener.start()
    secondListener.start()

    eventually(timeout(Span(2, Seconds)), interval(Span(300, Millis))) {
      secondListener.getPeers() should equal(Set(networkPeerOf(firstListener)))
      firstListener.getPeers() should equal(Set(networkPeerOf(secondListener)))
    }

    firstListener.stop()
    secondListener.stop()

  }

  private def networkPeerOf(listener: NetworkListener): NetworkPeer = {
    val configuration = listener.nodeConfiguration
    NetworkPeer(configuration.id, configuration.address, configuration.port)
  }


}
