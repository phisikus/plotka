package eu.phisikus.plotka.framework.fsm.events

import eu.phisikus.plotka.model.NetworkMessage
import eu.phisikus.plotka.network.talker.Talker

/**
  * This event represents incoming network message.
  * Additionally an instance of Talker is provided for eventual replies.
  *
  * @param incomingMessage message that was received
  * @param talker          instance of talker that can be used to send a reply
  */
class MessageEventWithTalker(override val incomingMessage: NetworkMessage, talker: Talker) extends MessageEvent(incomingMessage)

