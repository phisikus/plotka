package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.{AgentClient, Consul}
import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.framework.consul.ConsulServiceMapMatcher.containsService
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, Matchers}

class ConsulServiceRegistryManagerTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  private val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()

  private val testConfiguration = BasicNodeConfiguration(peers = List(), address = "127.0.0.1")
  private val consulUrl = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort
  private val testServiceName = "test-service"

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

  test("Should register service with consul properly") {
    val registryManager = new ConsulServiceRegistryManager(
      consulUrl,
      testServiceName,
      testConfiguration
    )
    registryManager.register()
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
    consulAgentClient.getServices should be('empty)
  }
}
