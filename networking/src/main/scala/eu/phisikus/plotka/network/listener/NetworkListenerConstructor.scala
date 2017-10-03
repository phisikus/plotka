package eu.phisikus.plotka.network.listener

import eu.phisikus.plotka.model.NetworkMessageConsumer

/**
  * It defines what a minimal builder for [[NetworkListener]] should provide and ask for.
  */
trait NetworkListenerConstructor {

  def build(): NetworkListener

  def withMessageHandler(newMessageConsumer: NetworkMessageConsumer): NetworkListenerConstructor

}
