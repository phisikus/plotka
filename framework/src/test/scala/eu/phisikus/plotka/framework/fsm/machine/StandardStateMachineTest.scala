package eu.phisikus.plotka.framework.fsm.machine

import eu.phisikus.plotka.framework.fsm.states.{FinalState, NormalState}
import eu.phisikus.plotka.framework.fsm.{Event, State}
import org.scalatest.{FunSuite, Matchers}

class StandardStateMachineTest extends FunSuite with Matchers {

  case class HandleTurnEvent() extends Event

  case class DoorsPushedEvent() extends Event

  case class DoorsKickedDown() extends Event

  case class Closed() extends NormalState {
    override def next(event: Event): Option[State] = {
      event match {
        case _: HandleTurnEvent => Some(Opened())
        case _: DoorsKickedDown => Some(Broken())
        case _ => Some(this)
      }
    }
  }

  case class Opened() extends NormalState {
    override def next(event: Event): Option[State] = {
      event match {
        case _: DoorsPushedEvent => Some(Closed())
        case _: DoorsKickedDown => Some(Broken())
        case _ => Some(this)
      }
    }
  }

  case class Broken() extends FinalState

  test("Should retrieve current state machine") {
    val initialState = Opened()
    val stateMachine = new StandardStateMachine(initialState)
    stateMachine.currentState shouldBe initialState
  }

  test("Should move from Opened to Closed state") {
    val initialState = Opened()
    val stateMachine = new StandardStateMachine(initialState)
    stateMachine.push(DoorsPushedEvent())
    stateMachine.currentState shouldEqual Closed()
  }

  test("Should transition through all the states") {
    val stateMachine = new StandardStateMachine(Opened())
    stateMachine.push(HandleTurnEvent())
    stateMachine.currentState shouldEqual Opened()
    stateMachine.push(DoorsPushedEvent())
    stateMachine.currentState shouldEqual Closed()
    stateMachine.push(DoorsKickedDown())
    stateMachine.currentState shouldEqual Broken()
  }

}
