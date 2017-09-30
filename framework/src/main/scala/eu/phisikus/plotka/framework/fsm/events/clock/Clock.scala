package eu.phisikus.plotka.framework.fsm.events.clock

/**
  * Represents clock that can emit ClockEvents.
  */
trait Clock {
  /**
    * Start counting time and - possibly - emitting ClockEvents
    */
  def start(): Unit

  /**
    * Stop the clock.
    */
  def stop(): Unit

}
