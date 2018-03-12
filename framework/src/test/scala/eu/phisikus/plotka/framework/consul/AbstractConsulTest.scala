package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.{AgentClient, Consul, KeyValueClient}
import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, Matchers}

abstract class AbstractConsulTest extends FunSuite
  with Matchers
  with Eventually
  with BeforeAndAfter
  with BeforeAndAfterAll {

  protected val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()

  protected val consulUrl: String = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort
  private val consul = Consul
    .builder()
    .withUrl(consulUrl)
    .build()

  protected val consulAgentClient: AgentClient = consul.agentClient()
  protected val consulKVClient: KeyValueClient = consul.keyValueClient()


  before {
    testConsul.reset()
  }

  override protected def afterAll(): Unit = {
    testConsul.close()
  }


}
