package eu.phisikus.plotka.framework.fsm.states

import eu.phisikus.plotka.framework.fsm.{Event, State}

/**
  * It represents a final state in which transitions to other states are not possible.
  */
abstract class FinalState extends State {
  override val isFinal: Boolean = true

  override def next(event: Event): Option[State] = None
}
