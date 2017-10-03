package eu.phisikus.plotka.framework.fsm.machine.networking

import eu.phisikus.plotka.conf.{NodeConfiguration, PeerConfiguration}
import eu.phisikus.plotka.framework.fsm.machine.StandardStateMachine
import eu.phisikus.plotka.framework.fsm.{Event, State, StateMachine}
import eu.phisikus.plotka.model.NetworkPeer
import eu.phisikus.plotka.network.listener.{ListenerController, NetworkListener, NetworkListenerBuilder}
import eu.phisikus.plotka.network.talker.NetworkTalker

import scala.annotation.tailrec
import scala.beans.BeanProperty


/**
  * Basic state machine with networking capabilities.
  * It uses standard implementation of [[StateMachine]] and [[NetworkListener]].
  * An instance of NetworkListener is created using provided node configuration.
  * An instance of StateMachine is created and bound with the listener.
  * After creation it should be started using [[ListenerController.start()]]
  *
  * @param initialState initial state of the StandardStateMachine
  * @param nodeConfig configuration for the NetworkListener
  */
class StandardNetworkStateMachine(initialState: State,
                                  nodeConfig: NodeConfiguration) extends ListenerController with StateMachine {

  private val localPeer: NetworkPeer = NetworkPeer(nodeConfig.id, nodeConfig.address, nodeConfig.port)
  private val networkTalker: NetworkTalker = new NetworkTalker(localPeer)
  private val stateMachine: StateMachine = new StandardStateMachine(initialState)

  private val listenerBuilder = NetworkListenerBuilder()
    .withAddress(nodeConfig.address)
    .withPort(nodeConfig.port)
    .withId(nodeConfig.id)

  private val networkListener: NetworkListener = withPeers(listenerBuilder, nodeConfig.peers)
    .withMessageHandler(new StateMachineNetworkMessageConsumer(stateMachine, networkTalker))
    .build()

  /**
    * Starts the listener
    */
  override def start(): Unit = {
    networkListener.start()
  }

  /**
    * Stops the listener
    */
  override def stop(): Unit = {
    networkListener.stop()
  }

  /**
    * Call this function to make state machine aware of a new event.
    *
    * @param event event to be published
    */
  override def push(event: Event): Unit = {
    stateMachine.push(event)
  }

  /**
    * @return current state of the machine
    */
  @BeanProperty
  override def currentState: State = {
    stateMachine.currentState
  }

  @tailrec
  private def withPeers(builder: NetworkListenerBuilder,
                        peers: List[PeerConfiguration]): NetworkListenerBuilder = {
    peers match {
      case head :: tail => withPeers(builder.withPeer(head), tail)
      case Nil => builder
    }
  }
}
