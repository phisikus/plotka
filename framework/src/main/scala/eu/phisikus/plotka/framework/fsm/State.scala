package eu.phisikus.plotka.framework.fsm

/**
  * This trait represents state in a finite-state machine (Mealy machine).
  *
  * @see [[StateMachine]]
  */
@FunctionalInterface
trait State {

  /**
    * @return true if current state is final
    */
  val isFinal: Boolean

  /**
    * This function performs transition to a next state and returns it.
    * New state is based on provided input.
    *
    * @param event input which is used to determine the next state
    * @return next state
    */
  def next(event: Event): Option[State]

}
