package eu.phisikus.plotka.framework.fsm.events

import eu.phisikus.plotka.framework.fsm.Event
import eu.phisikus.plotka.model.NetworkMessage

/**
  * This event represents incoming network message.
  *
  * @param incomingMessage message that was received
  */
case class MessageEvent(incomingMessage: NetworkMessage) extends Event

