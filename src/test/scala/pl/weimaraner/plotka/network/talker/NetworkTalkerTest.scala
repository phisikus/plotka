package pl.weimaraner.plotka.network.talker

import java.util.UUID

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model._
import pl.weimaraner.plotka.network.listener.Listener
import pl.weimaraner.plotka.network.listener.dto.TestMessage

class NetworkTalkerTest extends FunSuite with Eventually with Matchers {

  private val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3032)
  private val localPeer = NetworkPeer(
    testNodeConfiguration.id, testNodeConfiguration.address,
    testNodeConfiguration.port)


  test("Should send message using NetworkTalker") {
    val testMessageConsumer = new SingleMessageHandler
    val testListener = new Listener(testNodeConfiguration, testMessageConsumer)
    val testTalker = new NetworkTalker(localPeer)
    val testMessage = NetworkMessage(localPeer, localPeer, TestMessage(UUID.randomUUID().toString, 66))
    testListener.start()

    testTalker.send(testMessage.recipient.asInstanceOf[NetworkPeer], testMessage.message)

    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testMessageConsumer.receivedMessage should equal(testMessage)
    }
    testListener.stop()

  }

  class SingleMessageHandler extends NetworkMessageConsumer {
    var receivedMessage: Message[NetworkPeer, Peer, Serializable] = _

    override def consumeMessage(message: Message[NetworkPeer, Peer, Serializable]): Unit = {
      receivedMessage = message
    }
  }

}
