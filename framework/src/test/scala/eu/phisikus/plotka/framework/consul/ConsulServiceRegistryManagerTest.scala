package eu.phisikus.plotka.framework.consul

import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.framework.consul.ConsulServiceMapMatcher.containsService
import eu.phisikus.plotka.model.NetworkPeer
import org.scalatest.time.{Millis, Seconds, Span}

class ConsulServiceRegistryManagerTest extends AbstractConsulTest {

  private val testConfiguration = BasicNodeConfiguration(peers = List(), address = "127.0.0.1")
  private val testServiceName = "test-service"


  test("Should register service with consul properly") {
    val registryManager = new ConsulServiceRegistryManager(
      consulUrl,
      testServiceName,
      testConfiguration
    )
    registryManager.register()
    registryManager.shutdown()
    consulAgentClient.getServices should containsService(testServiceName, testConfiguration)
  }

  test("Should unregister service with consul properly") {
    val registryManager = new ConsulServiceRegistryManager(
      consulUrl,
      testServiceName,
      testConfiguration
    )
    registryManager.register()
    registryManager.unregister()
    registryManager.shutdown()
    consulAgentClient.getServices should be('empty)
  }

  test("Should get information about other peers") {
    val firstService = BasicNodeConfiguration(address = "127.0.0.2", peers = Nil)
    val secondService = BasicNodeConfiguration(address = "127.0.0.3", peers = Nil)
    val expectedPeerList = Set(
      NetworkPeer(firstService.id, firstService.address, firstService.port),
      NetworkPeer(secondService.id, secondService.address, secondService.port)
    )

    val firstRegistryManager = new ConsulServiceRegistryManager(
      consulUrl,
      testServiceName,
      firstService
    )
    val secondRegistryManager = new ConsulServiceRegistryManager(
      consulUrl,
      testServiceName,
      secondService
    )
    val managers = List(firstRegistryManager, secondRegistryManager)

    managers.foreach(rm => rm.register())
    eventually(timeout(Span(2, Seconds)), interval(Span(300, Millis))) {
      managers.head.getPeers() should equal(expectedPeerList)
    }
    managers.foreach(rm => rm.shutdown())
  }
}
