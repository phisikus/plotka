package eu.phisikus.plotka.framework.consul

import eu.phisikus.plotka.conf.model.BasicNodeConfiguration

import scala.io.Source

class ClusteredNetworkListenerBuilderTest extends AbstractConsulTest {

  val expectedConfiguration = BasicNodeConfiguration("node0.network", 3050, "127.0.0.1", Nil)
  lazy val testCustomConfigurationText: String = Source
    .fromResource("clustered_node.conf")
    .getLines
    .map(line => line + "\n")
    .mkString

  test("Should build network listener that loads configuration from consul") {
    consulKVClient.putValue("configuration", testCustomConfigurationText)
    val listener = ClusteredNetworkListenerBuilder()
      .withServiceName("testService")
      .withConsulUrl(consulUrl)
      .withConsulNodeConfiguration("configuration")
      .build()

    listener.nodeConfiguration should equal(expectedConfiguration)
  }

}
