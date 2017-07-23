package pl.weimaraner.plotka.network.consumer

import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.model.{Message, NetworkMessage, NetworkPeer, Peer}
import pl.weimaraner.plotka.network.listener.dto.TestMessage
import pl.weimaraner.plotka.network.talker.Talker

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
