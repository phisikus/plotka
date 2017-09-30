package eu.phisikus.plotka.framework.fsm.events.clock

import eu.phisikus.plotka.framework.fsm.Event

/**
  * Time-related event created by implementation of [[Clock]].
  *
  * @see [[Clock]]
  * @param sourceClock clock instance that generated this event
  */
case class ClockEvent(sourceClock: Clock) extends Event
