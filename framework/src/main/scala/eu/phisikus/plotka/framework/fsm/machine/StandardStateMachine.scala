package eu.phisikus.plotka.framework.fsm.machine

import eu.phisikus.plotka.framework.fsm.{Event, State, StateMachine}

class StandardStateMachine extends StateMachine {


  /**
    * Start the state machine using given initial state.
    *
    * @param initialState initial state
    */
  override def start(initialState: State): Unit = {
    //TODO implement
  }

  /**
    * Call this function to make state machine aware of a new event.
    *
    * @param event event to be published
    */
  override def push(event: Event): Unit = {
    //TODO implement
  }

  /**
    * Stop the state machine.
    */
  override def stop(): Unit = {
    //TODO implement
  }

  /**
    * Wait for the state machine to stop when it reaches any final state.
    */
  override def awaitTermination(): Unit = {
    //TODO implement
  }

}
