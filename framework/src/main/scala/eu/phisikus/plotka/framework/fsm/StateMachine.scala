package eu.phisikus.plotka.framework.fsm

/**
  * It represents a finite-state machine (Mealy machine)
  */
trait StateMachine {
  /**
    * Start the state machine using given initial state.
    *
    * @param initialState initial state
    */
  def start(initialState: State): Unit


  /**
    * Stop the state machine.
    */
  def stop(): Unit


  /**
    * Wait for the state machine to stop when it reaches any final state.
    */
  def awaitTermination(): Unit


  /**
    * Call this function to make state machine aware of a new event.
    *
    * @param event event to be published
    */
  def push(event: Event): Unit
}
