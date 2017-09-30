package eu.phisikus.plotka.framework.fsm.states

import eu.phisikus.plotka.framework.fsm.State


/**
  * It represents a typical, non-final state
  */
trait NormalState extends State {
  override val isFinal: Boolean = false
}

