package eu.phisikus.plotka.framework.fsm.events.clock

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

import eu.phisikus.plotka.framework.fsm.StateMachine

import scala.concurrent.duration.Duration

abstract class AbstractClock(stateMachine: StateMachine, duration: Duration) extends Clock {

  protected val threadPool = Executors.newSingleThreadExecutor()
  protected val isEnabled = new AtomicBoolean(true)


  /**
    * Start counting time and emitting ClockEvents
    */
  override def start(): Unit = {
    threadPool.execute(() => {
      clockLogic(stateMachine, duration)
    })
  }

  /**
    * Stop the clock.
    */
  override def stop(): Unit = {
    isEnabled.set(false)
    threadPool.shutdownNow()
  }

  def clockLogic(machine: StateMachine, duration: Duration): Unit

}
