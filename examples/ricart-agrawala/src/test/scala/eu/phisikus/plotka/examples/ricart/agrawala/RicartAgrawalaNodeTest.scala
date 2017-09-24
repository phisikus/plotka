package eu.phisikus.plotka.examples.ricart.agrawala

import java.util.concurrent.{CountDownLatch, ForkJoinPool}

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.providers.FileConfigurationProvider
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}

class RicartAgrawalaNodeTest extends FunSuite with Eventually with Matchers {

  private val logger = Logger("EntryPoint")
  private val configuration1Provider = new FileConfigurationProvider(Some("node1/application"))
  private val configuration2Provider = new FileConfigurationProvider(Some("node2/application"))
  private val node1Conf = configuration1Provider.loadConfiguration
  private val node2Conf = configuration2Provider.loadConfiguration

  test("Two nodes should execute their critical sections") {

    val executor = ForkJoinPool.commonPool()
    val barrier = new CountDownLatch(2)

    val testNode1 = new RicartAgrawalaNode(node1Conf, () => {
      logger.info("First node: I've reached my critical section! ")
      barrier.countDown()
    })

    val testNode2 = new RicartAgrawalaNode(node2Conf, () => {
      logger.info("Second node: I did my job!")
      barrier.countDown()
    })

    executor.execute(() => testNode1.start())
    executor.execute(() => testNode2.start())

    eventually(timeout(Span(5, Seconds)), interval(Span(300, Millis))) {
      barrier.await()
    }

    testNode1.stop()
    testNode2.stop()
  }

}
