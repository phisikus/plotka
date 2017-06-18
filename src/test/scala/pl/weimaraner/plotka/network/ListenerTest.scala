package pl.weimaraner.plotka.network

import java.io.ObjectOutputStream
import java.net.Socket

import org.scalatest.FunSuite
import pl.weimaraner.plotka.conf.model.BasicNodeConfiguration
import pl.weimaraner.plotka.model.{Message, NetworkMessageConsumer, NetworkPeer}

import scala.collection.mutable

class ListenerTest extends FunSuite {

  class QueueMessageHandler extends NetworkMessageConsumer {
    val receivedMessages: mutable.Queue[Message[_,_,_]] = mutable.Queue[Message[_,_,_]]()
    override def consumeMessage(message: Message[NetworkPeer, NetworkPeer, Serializable]): Unit = {
      receivedMessages.enqueue(message)
    }
  }

  test("Should start server socket and receive connections") {
    val testNodeConfiguration = BasicNodeConfiguration(peers = Nil)
    val testListener = new Listener(testNodeConfiguration, new QueueMessageHandler)
    testListener.startServerLoop()
    sendMessageToListener(testNodeConfiguration)

  }

  private def sendMessageToListener(testNodeConfiguration: BasicNodeConfiguration) = {
    val testClientSocket = new Socket(testNodeConfiguration.address, testNodeConfiguration.port)
    val objectOutputStream = new ObjectOutputStream(testClientSocket.getOutputStream)
    objectOutputStream.writeObject()
  }


}
