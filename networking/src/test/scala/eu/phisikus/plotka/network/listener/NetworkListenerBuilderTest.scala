package eu.phisikus.plotka.network.listener

import eu.phisikus.plotka.conf.model.BasicPeerConfiguration
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.listener.dto.TestMessage
import eu.phisikus.plotka.network.talker.NetworkTalker
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

class NetworkListenerBuilderTest extends FunSuite with Eventually with Matchers {

  test("Should build working test listener") {
    var messageReceived = false

    val localPeer = NetworkPeer("TEST1", "127.0.0.1", 3030)
    val testTalker = new NetworkTalker(localPeer)
    val testListener = NetworkListenerBuilder()
      .withId(localPeer.id)
      .withAddress(localPeer.address)
      .withPort(localPeer.port)
      .withPeer(BasicPeerConfiguration(localPeer.address, localPeer.port))
      .withMessageHandler(msg => messageReceived = true)
      .build()

    testListener.start()
    testTalker.send(localPeer, TestMessage())

    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      messageReceived shouldBe true
    }

    testListener.stop()
  }

}