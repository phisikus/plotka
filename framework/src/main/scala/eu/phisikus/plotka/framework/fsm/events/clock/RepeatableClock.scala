package eu.phisikus.plotka.framework.fsm.events.clock

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

import eu.phisikus.plotka.framework.fsm.StateMachine

import scala.annotation.tailrec
import scala.concurrent.duration.Duration

/**
  * This clock waits for a given amount of time and pushes [[ClockEvent]] to the state machine.
  * It starts counting when [[Clock.start()]] is executed and repeats it until [[Clock.stop()]] is called.
  *
  * @param stateMachine state machine that will receive the events
  * @param duration     amount of time between events
  */
class RepeatableClock(stateMachine: StateMachine, duration: Duration) extends Clock {
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
    val clockEvent = ClockEvent(this)
    stateMachine.push(clockEvent)
  }


}
