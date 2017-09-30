package eu.phisikus.plotka.framework.fsm.events.clock

import java.util.concurrent.TimeUnit

import eu.phisikus.plotka.framework.fsm.StateMachine
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Milliseconds, Span}

import scala.concurrent.duration.Duration

class RepeatableClockTest extends FunSuite with MockitoSugar with Eventually {

  test("Should push clock event to the state machine after 1s") {
    testClockForDuration(1, 1, TimeUnit.SECONDS)
  }

  test("Should push multiple clock events to the state machine") {
    testClockForDuration(10, 100, TimeUnit.MILLISECONDS)
  }

  private def testClockForDuration(count: Int, time: Long, timeUnit: TimeUnit): Unit = {
    val timeDuration = Duration(time, timeUnit)
    val limitTimeInMillis = count * timeDuration.toMillis * 1.2
    val waitSpan = Span(limitTimeInMillis, Milliseconds)
    val stateMachine = mock[StateMachine]
    val singleAlarmClock = new RepeatableClock(stateMachine, timeDuration)

    singleAlarmClock.start()
    eventually(timeout(waitSpan)) {
      verify(stateMachine, atLeast(count)).push(ClockEvent(singleAlarmClock))
    }
    singleAlarmClock.stop()
  }
}
