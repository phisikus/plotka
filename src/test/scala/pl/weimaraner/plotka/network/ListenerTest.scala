package pl.weimaraner.plotka.network

import java.io.{ByteArrayOutputStream, ObjectOutputStream, OutputStream}
import java.net.Socket
import java.nio.ByteBuffer
import java.util.UUID

import org.apache.commons.lang3.{RandomStringUtils, RandomUtils}
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model._
import pl.weimaraner.plotka.network.dto.TestMessage

import scala.annotation.tailrec
import scala.collection.mutable

class ListenerTest extends FunSuite with Eventually with Matchers {


  test("Should start listener and receive message") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil)
    val testMessageConsumer = new QueueMessageHandler
    val expectedMessage: Message[Peer, Peer, Serializable] = getTestMessage(testNodeConfiguration)
    val testListener = new Listener(testNodeConfiguration, () => new SessionState(), testMessageConsumer)

    testListener.start()
    sendMessageToListener(testNodeConfiguration)

    eventually {
      testMessageConsumer.receivedMessages.dequeue() should equal(expectedMessage)
    }
    testListener.stop()
  }




  test("Should start listener and receive multiple messages") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil)
    val testMessageConsumer = new QueueMessageHandler
    val testMessages : List[NetworkMessage] = getTestMessages(100)
    val testListener = new Listener(testNodeConfiguration, () => new SessionState(), testMessageConsumer)

    testListener.start()
    sendMessagesToListener(testNodeConfiguration, testMessages)

    eventually {
      testMessageConsumer.receivedMessages should contain allElementsOf(testMessages)
    }
    testListener.stop()
  }

  def getTestMessages(count: Int): List[NetworkMessage] = {
    val randomMessageBuilder = (i : Int) => {
      val sender = NetworkPeer(getRandomString, getRandomString, RandomUtils.nextInt())
      val recipient = NetworkPeer(getRandomString, getRandomString, RandomUtils.nextInt())
      NetworkMessage(sender, recipient, TestMessage(getRandomString))
    }

    1.to(count)
      .map(randomMessageBuilder).toList
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

  class QueueMessageHandler extends NetworkMessageConsumer {
    val receivedMessages: mutable.Queue[Message[NetworkPeer, NetworkPeer, Serializable]] = mutable.Queue()

    override def consumeMessage(message: Message[NetworkPeer, NetworkPeer, Serializable]): Unit = {
      receivedMessages.enqueue(message)
    }

  }


}
