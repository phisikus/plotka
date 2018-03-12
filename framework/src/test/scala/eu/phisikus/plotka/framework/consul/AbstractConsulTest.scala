package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.{AgentClient, Consul}
import com.pszymczyk.consul.{ConsulProcess, ConsulStarterBuilder}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, Matchers}

abstract class AbstractConsulTest extends FunSuite
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll {

  protected val testConsul: ConsulProcess = ConsulStarterBuilder
    .consulStarter
    .build
    .start()

  protected val consulUrl: String = "http://" + testConsul.getAddress + ":" + testConsul.getHttpPort
  protected val consulAgentClient: AgentClient = Consul
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


}
