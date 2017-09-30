package eu.phisikus.plotka.framework.fsm.events.clock

import eu.phisikus.plotka.framework.fsm.StateMachine

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * This clock pushes [[ClockEvent]] to provided state machine every time defined moment passes.
  * The clock works once [[Clock.start()]] is called and stops if [[Clock.stop()]] is executed.
  *
  * @param stateMachine state machine that will receive the events
  * @param duration     amount of time between events emission
  */
class RepeatableClock(stateMachine: StateMachine, duration: Duration)
  extends AbstractClock(stateMachine, duration) {

  override def clockLogic(machine: StateMachine, duration: Duration): Unit = {
    clockLoop(stateMachine, duration)
  }

  @tailrec
  private def clockLoop(stateMachine: StateMachine, duration: Duration): Unit = {
    waitAndEmitEvent(stateMachine, duration)
    if (isEnabled.get()) {
      clockLoop(stateMachine, duration)
    }
  }

  private def waitAndEmitEvent(stateMachine: StateMachine, duration: Duration): Unit = {
    sleep(duration)
    if (isEnabled.get()) {
      stateMachine.push(ClockEvent(this))
    }
  }

  private def sleep(duration: Duration) = {
    try {
      Thread.sleep(duration.toMillis)
    } catch {
      case _: InterruptedException =>
    }
  }
}
