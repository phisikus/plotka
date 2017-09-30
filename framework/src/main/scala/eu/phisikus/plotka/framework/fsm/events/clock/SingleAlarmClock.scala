package eu.phisikus.plotka.framework.fsm.events.clock

import eu.phisikus.plotka.framework.fsm.StateMachine

import scala.concurrent.duration.Duration

/**
  * This clock pushes one [[ClockEvent]] to provided state machine after defined time passes.
  *
  * @param stateMachine state machine that will receive the event
  * @param duration     amount of time between [[Clock.start()]] call and event emission
  */
class SingleAlarmClock(stateMachine: StateMachine, duration: Duration)
  extends AbstractClock(stateMachine, duration) {

  override def clockLogic(machine: StateMachine, duration: Duration): Unit = {
    if (isEnabled.get()) {
      Thread.sleep(duration.toMillis)
      stateMachine.push(ClockEvent(this))
    }
  }
}
