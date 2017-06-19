package pl.weimaraner.plotka.network

import java.io.ObjectOutputStream
import java.net.Socket

import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSuite, Matchers}
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model._
import pl.weimaraner.plotka.network.dto.TestMessage

import scala.collection.mutable

class ListenerTest extends FunSuite with Eventually with Matchers {



  test("Should start server socket and receive message") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil)
    val testMessageConsumer = new QueueMessageHandler
    val expectedMessage: Message[Peer, Peer, Serializable] = getTestMessage(testNodeConfiguration)
    val testListener = new Listener(testNodeConfiguration, testMessageConsumer)

    testListener.startServerLoop()
    sendMessageToListener(testNodeConfiguration)

    eventually {
      testMessageConsumer.receivedMessages.dequeue() should equal(expectedMessage)
    }
  }

  private def sendMessageToListener(testNodeConfiguration: BasicNodeConfiguration) = {
    val testClientSocket = new Socket(testNodeConfiguration.address, testNodeConfiguration.port)
    val objectOutputStream = new ObjectOutputStream(testClientSocket.getOutputStream)
    val testMessage: NetworkMessage = getTestMessage(testNodeConfiguration)
    objectOutputStream.writeObject(testMessage)
    objectOutputStream.close()
    testClientSocket.close()
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
