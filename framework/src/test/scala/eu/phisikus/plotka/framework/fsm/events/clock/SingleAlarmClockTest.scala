package eu.phisikus.plotka.framework.fsm.events.clock


import java.util.concurrent.TimeUnit

import eu.phisikus.plotka.framework.fsm.StateMachine
import org.mockito.Mockito.verify
import org.mockito.internal.verification.Times
import org.scalatest.FunSuite
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Milliseconds, Span}

import scala.concurrent.duration.Duration

class SingleAlarmClockTest extends FunSuite with MockitoSugar with Eventually {
  test("Should push single clock event to the state machine") {
    val stateMachine = mock[StateMachine]
    val singleAlarmClock = new SingleAlarmClock(stateMachine, Duration(10, TimeUnit.MILLISECONDS))

    singleAlarmClock.start()
    eventually(timeout(Span(300, Milliseconds))) {
      verify(stateMachine, new Times(1)).push(ClockEvent(singleAlarmClock))
    }
    singleAlarmClock.stop()
  }
}
