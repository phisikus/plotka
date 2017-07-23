package pl.weimaraner.plotka.network.listener

import java.io.{ByteArrayOutputStream, ObjectOutputStream, OutputStream}
import java.net.Socket
import java.nio.ByteBuffer
import java.util.UUID

import org.apache.commons.lang3.RandomUtils
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model._
import pl.weimaraner.plotka.network.listener.dto.TestMessage
import pl.weimaraner.plotka.network.listener.handlers.ListMessageHandler

import scala.annotation.tailrec

class NetworkListenerTest extends FunSuite with Eventually with Matchers {

  private val TestMessageCount = 1000
  private val testTimeout = timeout(Span(60, Seconds))
  private val testCheckInterval = interval(Span(300, Millis))

  test("Should start listener and receive message") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3031)
    val testMessageConsumer = new ListMessageHandler
    val expectedMessage: Message[Peer, Peer, Serializable] = getTestMessage(testNodeConfiguration)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)

    testListener.start()
    sendMessageToListener(testNodeConfiguration)

    eventually {
      testMessageConsumer.receivedMessages.head should equal(expectedMessage)
    }
    testListener.stop()
  }

  test("Should start listener and receive multiple messages") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3032)
    val testMessageConsumer = new ListMessageHandler
    val testMessages: List[NetworkMessage] = getTestMessages(TestMessageCount)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)

    testListener.start()
    sendMessagesToListener(testNodeConfiguration, testMessages)

    eventually(testTimeout, testCheckInterval) {
      testMessageConsumer.receivedMessages should contain allElementsOf testMessages
    }
    testListener.stop()
  }

  test("Should start listener and receive multiple messages from multiple connections") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil, port = 3033)
    val testMessageConsumer = new ListMessageHandler
    val testMessages: List[NetworkMessage] = getTestMessages(TestMessageCount)
    val testListener = new NetworkListener(testNodeConfiguration, testMessageConsumer)

    testListener.start()
    testMessages.par.foreach(msg => {
      sendMessagesToListener(testNodeConfiguration, List(msg))
    })

    eventually(testTimeout, testCheckInterval) {
      testMessageConsumer.receivedMessages should contain allElementsOf testMessages
    }
    testListener.stop()
  }


  def getTestMessages(count: Int): List[NetworkMessage] = {
    val randomMessageBuilder = (i: Int) => {
      val sender = NetworkPeer(getRandomString, getRandomString, RandomUtils.nextInt())
      val recipient = NetworkPeer(getRandomString, getRandomString, RandomUtils.nextInt())
      NetworkMessage(sender, recipient, TestMessage(getRandomString))
    }

    Range(0, count).map(randomMessageBuilder).toList
  }


  private def getRandomString = {
    UUID.randomUUID().toString
  }

  def getIntAsBytes(number: Int): Array[Byte] = {
    val intBuffer = ByteBuffer.allocate(4)
    intBuffer.putInt(number)
    intBuffer.array()
  }

  private def sendMessageToListener(testNodeConfiguration: BasicNodeConfiguration) = {
    val testMessage: NetworkMessage = getTestMessage(testNodeConfiguration)
    sendMessagesToListener(testNodeConfiguration, List(testMessage))
  }

  private def sendMessagesToListener(testNodeConfiguration: BasicNodeConfiguration, testMessages: List[NetworkMessage]) = {
    val testClientSocket = new Socket(testNodeConfiguration.address, testNodeConfiguration.port)
    val clientOutputStream = testClientSocket.getOutputStream
    writeMessages(clientOutputStream, testMessages)
    clientOutputStream.flush()
    clientOutputStream.close()
    testClientSocket.close()
  }

  @tailrec
  private def writeMessages(clientOutputStream: OutputStream, testMessages: List[NetworkMessage]): Unit = {
    testMessages match {
      case Nil =>
        clientOutputStream.write(getIntAsBytes(0))
      case msg :: tail =>
        val serializedMessage = getMessageAsBytes(msg)
        clientOutputStream.write(getIntAsBytes(serializedMessage.length))
        clientOutputStream.write(serializedMessage)
        writeMessages(clientOutputStream, tail)
    }
  }

  private def getMessageAsBytes(testMessage: NetworkMessage): Array[Byte] = {
    val byteOutputStream = new ByteArrayOutputStream()
    val objectStream = new ObjectOutputStream(byteOutputStream)
    objectStream.writeObject(testMessage)
    objectStream.flush()
    objectStream.close()
    byteOutputStream.close()
    byteOutputStream.toByteArray
  }

  private def getTestMessage(testNodeConfiguration: BasicNodeConfiguration) = {
    val testPeer = NetworkPeer("eb1b35ad-7001-4e5f-8c3a-487255713a0c", testNodeConfiguration.address, testNodeConfiguration.port)
    val testMessage = NetworkMessage(testPeer, testPeer, TestMessage())
    testMessage
  }


}
