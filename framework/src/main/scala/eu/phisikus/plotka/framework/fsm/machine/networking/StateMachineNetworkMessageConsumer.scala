package eu.phisikus.plotka.framework.fsm.machine.networking

import eu.phisikus.plotka.framework.fsm.StateMachine
import eu.phisikus.plotka.framework.fsm.events.MessageEventWithTalker
import eu.phisikus.plotka.model._
import eu.phisikus.plotka.network.talker.Talker

/**
  * This is used to bind incoming messages to state machine network events.
  * Each [[Message]] with given instance of [[Talker]] is formed into [[MessageEventWithTalker]]
  *
  * @param stateMachine state machine which will receive events
  * @param talker [[Talker]] that will be passed with the incoming message
  */
class StateMachineNetworkMessageConsumer(stateMachine: StateMachine, talker: Talker)
  extends NetworkMessageConsumer {
  override def consumeMessage(message: Message[NetworkPeer, Peer, Serializable]): Unit = {
    stateMachine.push(new MessageEventWithTalker(message.asInstanceOf[NetworkMessage], talker))
  }
}
