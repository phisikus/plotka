package pl.weimaraner.plotka.network

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.net.Socket
import java.nio.ByteBuffer

import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model._
import pl.weimaraner.plotka.network.dto.TestMessage

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

  private def sendMessageToListener(testNodeConfiguration: BasicNodeConfiguration) = {
    val testMessage: NetworkMessage = getTestMessage(testNodeConfiguration)
    val testClientSocket = new Socket(testNodeConfiguration.address, testNodeConfiguration.port)
    val clientOutputStream = testClientSocket.getOutputStream

    val serializedMessage = getMessageAsBytes(testMessage)
    clientOutputStream.write(getIntAsBytes(serializedMessage.length))
    clientOutputStream.write(serializedMessage)
    clientOutputStream.write(getIntAsBytes(0))
    clientOutputStream.flush()
    clientOutputStream.close()
    testClientSocket.close()
  }

  def getIntAsBytes(number: Int): Array[Byte] = {
    val intBuffer = ByteBuffer.allocate(4)
    intBuffer.putInt(number)
    intBuffer.array()
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
