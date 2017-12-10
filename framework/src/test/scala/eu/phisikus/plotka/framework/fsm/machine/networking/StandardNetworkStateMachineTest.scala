package eu.phisikus.plotka.framework.fsm.machine.networking

import eu.phisikus.plotka.conf.model.{BasicNodeConfiguration, BasicPeerConfiguration}
import eu.phisikus.plotka.framework.fsm.events.MessageEventWithTalker
import eu.phisikus.plotka.framework.fsm.states.{FinalState, NormalState}
import eu.phisikus.plotka.framework.fsm.{Event, State}
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.talker.NetworkTalker
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

class StandardNetworkStateMachineTest extends FunSuite with Matchers with Eventually {


  /**
    * Test configuration
    */
  private val nodeConf = BasicNodeConfiguration(
    peers = List(BasicPeerConfiguration("127.0.0.2"))
  )
  private val localPeer = NetworkPeer(nodeConf.id, nodeConf.address, nodeConf.port)
  private val networkTalker = new NetworkTalker(localPeer)

  /**
    * Test states
    */
  case class MessageDelivered(message: TestMessage) extends FinalState

  case class WaitingForMessage() extends NormalState {
    override def next(event: Event): Option[State] = {
      event match {
        case messageEvent: MessageEventWithTalker =>
          val messageContents = messageEvent.incomingMessage.message.asInstanceOf[TestMessage]
          val newState = MessageDelivered(messageContents)
          Some(newState)
        case _ => None
      }
    }
  }

  case class SomeState() extends NormalState {
    override def next(event: Event): Option[State] = {
      Some(EndState())
    }
  }

  case class EndState() extends FinalState

  case class SomeEvent() extends Event


  test("Should receive message and change state") {
    val stateMachine = new StandardNetworkStateMachine(WaitingForMessage(), nodeConf)
    stateMachine.start()
    stateMachine.currentState shouldEqual WaitingForMessage()
    networkTalker.send(localPeer, TestMessage("Hello!"))

    eventually(timeout(Span(5, Seconds)), interval(Span(300, Millis))) {
      stateMachine.currentState shouldEqual MessageDelivered(TestMessage("Hello!"))
    }
    stateMachine.stop()

  }

  test("Should react to pushed events") {
    val stateMachine = new StandardNetworkStateMachine(SomeState(), nodeConf)
    stateMachine.push(SomeEvent())
    stateMachine.currentState shouldEqual EndState()
  }

}
