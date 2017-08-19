package eu.phisikus.plotka.network.talker

import java.util.UUID

import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.model.{NetworkMessage, NetworkPeer}
import eu.phisikus.plotka.network.listener.NetworkListener
import eu.phisikus.plotka.network.listener.dto.TestMessage
import eu.phisikus.plotka.network.listener.handlers.ListMessageHandler
import org.apache.commons.lang3.RandomUtils
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

class NetworkTalkerTest extends FunSuite with Eventually with Matchers {

  private val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3034)
  private val localPeer = NetworkPeer(
    testNodeConfiguration.id, testNodeConfiguration.address,
    testNodeConfiguration.port)
  private val testMessageConsumer = new ListMessageHandler


  test("Should send message using NetworkTalker") {
    val testTalker = new NetworkTalker(localPeer)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)
    val testMessage = NetworkMessage(localPeer, localPeer, getRandomTestMessageBody)
    testListener.start()

    val result = testTalker.send(testMessage.recipient.asInstanceOf[NetworkPeer], testMessage.message)

    assert(result.isSuccess)
    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testMessageConsumer.receivedMessages should contain(testMessage)
    }
    testListener.stop()

  }

  test("Should send message asynchronously using NetworkTalker") {
    val testTalker = new NetworkTalker(localPeer)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)
    val testMessage = NetworkMessage(localPeer, localPeer, getRandomTestMessageBody)
    testListener.start()

    var wasCallbackSuccessful = false
    testTalker.send(testMessage.recipient.asInstanceOf[NetworkPeer],
      testMessage.message,
      sendResult => wasCallbackSuccessful = sendResult.isSuccess)

    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testMessageConsumer.receivedMessages should contain(testMessage)
      wasCallbackSuccessful shouldBe true
    }
    testListener.stop()

  }

  test("Should send multiple messages using NetworkTalker") {
    val testTalker = new NetworkTalker(localPeer)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)
    val testMessages = getMultipleRandomTestMessages(10000)
    testListener.start()

    testMessages.par.map(testMessage => {
      testTalker.send(testMessage.recipient.asInstanceOf[NetworkPeer], testMessage.message)
    }).foreach(sendResult => assert(sendResult.isSuccess))


    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testMessageConsumer.receivedMessages should contain allElementsOf testMessages
    }
    testListener.stop()

  }

  private def getMultipleRandomTestMessages(count: Int): List[NetworkMessage] = {
    Range(0, count).map(i => NetworkMessage(
      localPeer,
      localPeer,
      getRandomTestMessageBody)).toList

  }

  private def getRandomTestMessageBody: TestMessage = {
    TestMessage(UUID.randomUUID().toString, RandomUtils.nextInt())
  }

}