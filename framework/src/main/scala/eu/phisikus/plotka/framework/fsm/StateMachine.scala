package eu.phisikus.plotka.framework.fsm

import scala.beans.BeanProperty

/**
  * It represents a finite-state machine (Mealy machine)
  */
trait StateMachine {

  /**
    * Call this function to make state machine aware of a new event.
    *
    * @param event event to be published
    */
  def push(event: Event): Unit


  /**
    * @return current state of the machine
    */
  @BeanProperty def currentState: State
}
