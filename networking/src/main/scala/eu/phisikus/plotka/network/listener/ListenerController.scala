package eu.phisikus.plotka.network.listener

/**
  * Provides basic start/stop operations for the Listener
  */
trait ListenerController {

  /**
    * Starts the listener
    */
  def start() : Unit


  /**
    * Stops the listener
    */
  def stop() : Unit

}
