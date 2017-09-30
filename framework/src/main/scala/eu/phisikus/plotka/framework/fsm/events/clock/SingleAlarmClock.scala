package eu.phisikus.plotka.framework.fsm.events.clock

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

import eu.phisikus.plotka.framework.fsm.StateMachine

import scala.annotation.tailrec
import scala.concurrent.duration.Duration

/**
  * This clock waits for a given amount of time and pushes ClockEvent to state machine.
  *
  * @param stateMachine machine that will receive the event
  * @param duration     amount of time between [[Clock.start()]] execution and ClockEvent creation.
  */
class SingleAlarmClock(stateMachine: StateMachine, duration: Duration) extends Clock {

  private val threadPool = Executors.newSingleThreadExecutor()
  private val isEnabled = new AtomicBoolean(true)

  /**
    * Start counting time and emitting ClockEvents
    */
  override def start(): Unit = {
    threadPool.execute(() => {
      clockLoop(stateMachine, duration)
    })
  }

  /**
    * Stop the clock.
    */
  override def stop(): Unit = {
    isEnabled.set(false)
  }

  @tailrec
  private def clockLoop(stateMachine: StateMachine, duration: Duration): Unit = {
    waitAndEmitEvent(stateMachine, duration)
    if (isEnabled.get()) {
      clockLoop(stateMachine, duration)
    }
  }

  private def waitAndEmitEvent(stateMachine: StateMachine, duration: Duration): Unit = {
    Thread.sleep(duration.toMillis)
    stateMachine.push(ClockEvent(this))
  }


}
