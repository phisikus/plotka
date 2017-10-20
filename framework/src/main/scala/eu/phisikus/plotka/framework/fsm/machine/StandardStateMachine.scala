package eu.phisikus.plotka.framework.fsm.machine

import java.util.concurrent.atomic.AtomicReference

import eu.phisikus.plotka.framework.fsm.{Event, State, StateMachine}

import scala.beans.BeanProperty

/**
  * Basic implementation of the finite-state machine.
  *
  * @param initialState initial state that the machine is in.
  */
class StandardStateMachine(@BeanProperty initialState: State) extends StateMachine {

  val state = new AtomicReference(initialState)

  /**
    * Call this function to make state machine aware of a new event.
    *
    * @param event event to be published
    */
  override def push(event: Event): Unit = {
    state.synchronized {
      state.set(transition(state.get(), event))
    }
  }

  private def transition(oldState: State, event: Event): State = {
    oldState.next(event) match {
      case Some(newState) => newState
      case None => oldState
    }
  }

  /**
    * @return current state of the machine
    */
  override def currentState: State = state.get()
}
