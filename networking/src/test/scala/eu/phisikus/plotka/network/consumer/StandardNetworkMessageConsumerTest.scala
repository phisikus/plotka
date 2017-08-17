package eu.phisikus.plotka.network.consumer

import eu.phisikus.plotka.model.{Message, NetworkMessage, NetworkPeer, Peer}
import eu.phisikus.plotka.network.listener.dto.TestMessage
import eu.phisikus.plotka.network.talker.Talker
import org.scalatest.{FunSuite, Matchers}

class StandardNetworkMessageConsumerTest extends FunSuite with Matchers {

  private val testPeer = new NetworkPeer("127.0.0.1", 9090)

  test("Create working message consumer") {
    val expectedMessage = NetworkMessage(testPeer, testPeer, TestMessage("TRUE"))
    var actualMessage: NetworkMessage = NetworkMessage(testPeer, testPeer, TestMessage("FALSE"))
    val testMessageHandler = (message: NetworkMessage, talker: Talker) => {
      actualMessage = message
    }

    val testNetworkMessageConsumer = new StandardNetworkMessageConsumer(
      testPeer,
      testMessageHandler
    )

    testNetworkMessageConsumer.consumeMessage(
      expectedMessage.asInstanceOf[Message[NetworkPeer, Peer, Serializable]]
    )

    assert(actualMessage == expectedMessage)
  }

}
