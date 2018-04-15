package eu.phisikus.plotka.network.talker

import java.util.UUID
import java.util.concurrent.TimeUnit

import eu.phisikus.plotka.conf.model.BasicNodeConfiguration
import eu.phisikus.plotka.model.{NetworkMessage, NetworkPeer}
import eu.phisikus.plotka.network.listener.NetworkListener
import eu.phisikus.plotka.network.listener.dto.TestMessage
import eu.phisikus.plotka.network.listener.handlers.ListMessageHandler
import org.apache.commons.lang3.RandomUtils
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class NetworkTalkerTest extends FunSuite with Eventually with Matchers {

  private val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3034)
  private val localPeer = NetworkPeer(
    testNodeConfiguration.id, testNodeConfiguration.address,
    testNodeConfiguration.port)

  test("Should send message using NetworkTalker") {
    val testMessageConsumer = new ListMessageHandler()
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
    val testMessageConsumer = new ListMessageHandler()
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
    val testMessageConsumer = new ListMessageHandler()
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

  test("Should fail on sending message to peer that is not listening") {
    val testTalker = new NetworkTalker(localPeer)
    val testMessage = NetworkMessage(localPeer, localPeer, getRandomTestMessageBody)
    val sendResult = testTalker.send(NetworkPeer("fake", "127.0.0.2", 9090), testMessage)
    sendResult.isFailure shouldBe true
  }

  test("Should fail on sending message to peer that disconnected") {
    val testMessageConsumer = new ListMessageHandler()
    val testTalker = new NetworkTalker(localPeer)
    val testMessage = NetworkMessage(localPeer, localPeer, getRandomTestMessageBody)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)
    testListener.start()
    testTalker.send(localPeer, testMessage)
    testListener.stop()
    eventually(timeout(Span(10, Seconds)), interval(Span(300, Millis))) {
      testTalker.send(localPeer, testMessage).isFailure shouldBe true
    }
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
