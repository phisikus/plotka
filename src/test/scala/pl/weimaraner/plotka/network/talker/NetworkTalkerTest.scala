package pl.weimaraner.plotka.network.talker

import java.util.UUID

import org.apache.commons.lang3.RandomUtils
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model._
import pl.weimaraner.plotka.network.listener.Listener
import pl.weimaraner.plotka.network.listener.dto.TestMessage
import pl.weimaraner.plotka.network.listener.utils.QueueMessageHandler

class NetworkTalkerTest extends FunSuite with Eventually with Matchers {

  private val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3034)
  private val localPeer = NetworkPeer(
    testNodeConfiguration.id, testNodeConfiguration.address,
    testNodeConfiguration.port)


  test("Should send message using NetworkTalker") {
    val testMessageConsumer = new QueueMessageHandler
    val testListener = new Listener(testNodeConfiguration, testMessageConsumer)
    val testTalker = new NetworkTalker(localPeer)
    val testMessage = NetworkMessage(localPeer, localPeer, getRandomTestMessageBody)
    testListener.start()

    testTalker.send(testMessage.recipient.asInstanceOf[NetworkPeer], testMessage.message)

    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testMessageConsumer.receivedMessages should contain (testMessage)
    }
    testListener.stop()

  }

  test("Should send multiple messages using NetworkTalker") {
    val testMessageConsumer = new QueueMessageHandler
    val testListener = new Listener(testNodeConfiguration, testMessageConsumer)
    val testTalker = new NetworkTalker(localPeer)
    val testMessages = getMultipleRandomTestMessages(1000)
    testListener.start()

    testMessages.foreach(testMessage => {
      testTalker.send(testMessage.recipient.asInstanceOf[NetworkPeer], testMessage.message)
    })


    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testMessageConsumer.receivedMessages should contain allElementsOf testMessages
    }
    testListener.stop()

  }

  private def getMultipleRandomTestMessages(count: Int): List[NetworkMessage] = {
    count match {
      case x if x > 0 => NetworkMessage(
        localPeer,
        localPeer,
        getRandomTestMessageBody) :: getMultipleRandomTestMessages(count - 1)
      case 0 => Nil
    }
  }

  private def getRandomTestMessageBody: TestMessage = {
    TestMessage(UUID.randomUUID().toString, RandomUtils.nextInt())
  }

}
