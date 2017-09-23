package eu.phisikus.plotka.examples.ricart.agrawala

import java.util.concurrent.atomic.AtomicLong

import com.typesafe.scalalogging.Logger
import eu.phisikus.plotka.conf.{NodeConfiguration, PeerConfiguration}
import eu.phisikus.plotka.examples.ricart.agrawala.message.{AcceptMessage, RequestMessage}
import eu.phisikus.plotka.examples.ricart.agrawala.model.{Accept, Request}
import eu.phisikus.plotka.model.{NetworkMessage, NetworkMessageConsumer, NetworkPeer}
import eu.phisikus.plotka.network.listener.NetworkListener
import eu.phisikus.plotka.network.talker.{NetworkTalker, Talker}

import scala.collection.mutable.ListBuffer

class RicartAgrawalaNode(nodeConfiguration: NodeConfiguration, forCriticalSectionExecution: () => Unit) {
  private val logger = Logger("RicartAgrawala:" + nodeConfiguration.id)
  private val currentClock: AtomicLong = new AtomicLong(0)
  private val myself = NetworkPeer(nodeConfiguration.id, nodeConfiguration.address, nodeConfiguration.port)
  private val talker = new NetworkTalker(myself)
  private val messageHandler: NetworkMessageConsumer = inputMessage => {
    val msg = inputMessage.asInstanceOf[NetworkMessage]
    val sender = msg.sender.asInstanceOf[NetworkPeer]
    logger.info(s"Received message: ${msg.message} from $sender")
    msg.message match {
      case request: RequestMessage =>
        val incommingRequest = Request(sender, request.timestamp)
        processRequest(incommingRequest, talker)
      case agreement: AcceptMessage =>
        val acceptance = Accept(sender, agreement.timestamp)
        processAcceptance(acceptance, talker)
    }
  }
  private val nodeListener = new NetworkListener(nodeConfiguration, messageHandler)
  private val waitingRequests = ListBuffer[Request]()
  private val agreements = ListBuffer[Accept]()
  private var currentRequest: Option[Request] = Option.empty

  def start(): Unit = {
    nodeListener.start()
    repeatWhileWithDelay {
      requestCriticalSection()
      true
    }
  }

  def requestCriticalSection(): Unit = {
    val requestClockValue = currentClock.incrementAndGet()
    val requestMessage = RequestMessage(requestClockValue)
    val myRequest = Request(myself, requestClockValue)
    this.synchronized {
      currentRequest = Some(myRequest)
    }
    nodeConfiguration.peers.foreach(peer => {
      val recipient = new NetworkPeer(peer.address, peer.port)
      logger.info("Sending: {} to {}", requestMessage, recipient)
      repeatWhileWithDelay({
        talker.send(recipient, requestMessage).isFailure
      })
    })
  }

  def stop(): Unit = {
    nodeListener.stop()
  }

  private def processRequest(request: Request, talker: Talker): Unit = {
    this.synchronized {
      updateLocalClock(request.timestamp)
      val shouldAccept = currentRequest match {
        case Some(myRequest) => myRequest.isBetterThan(request)
        case None => false
      }
      if (shouldAccept) {
        sendAccept(request, talker)
      } else {
        waitingRequests += request
      }
    }
  }

  private def sendAccept(request: Request, talker: Talker) = {
    val newClockValue = currentClock.incrementAndGet()
    repeatWhileWithDelay {
      val message = AcceptMessage(newClockValue)
      logger.info("Sending: {} to {}", message, request.sender)
      talker.send(request.sender, message).isFailure
    }
  }

  private def repeatWhileWithDelay(condition: => Boolean): Unit = {
    while (condition) {
      Thread.sleep(100L)
    }
  }

  private def updateLocalClock(timestamp: Long) = {
    currentClock.accumulateAndGet(timestamp, (currentValue, t) => Math.max(currentValue, t + 1))
  }

  private def processAcceptance(agreement: Accept, talker: Talker): Unit = {
    this.synchronized {
      updateLocalClock(agreement.timestamp)
      agreements += agreement
      if (enterCriticalSection(nodeConfiguration.peers, agreements.toList)) {
        sendAcceptToOtherNodes(waitingRequests.toList, talker)
        waitingRequests.clear()
      }
    }

  }

  private def sendAcceptToOtherNodes(waitingList: List[Request], talker: Talker): Unit = {
    val newClockValue = currentClock.incrementAndGet()
    waitingList
      .map(request => request.sender)
      .foreach(recipient => {
        val message = AcceptMessage(newClockValue)
        logger.info("Sending: {} to {}", message, recipient)
        repeatWhileWithDelay {
          talker.send(recipient, message).isFailure
        }
      })
  }

  private def enterCriticalSection(peers: List[PeerConfiguration], agreements: List[Accept]): Boolean = {
    val nodesThatAgreed = agreements
      .map(agreement => agreement.sender)
      .map(sender => (sender.address, sender.port))
      .toSet

    val knownNodes = peers
      .map(peer => (peer.address, peer.port))
      .toSet

    if (knownNodes == nodesThatAgreed) {
      currentRequest = Option.empty
      forCriticalSectionExecution.apply()
      true
    } else {
      false
    }
  }

}
