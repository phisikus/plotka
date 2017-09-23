package eu.phisikus.plotka.examples.ricart.agrawala.model

import eu.phisikus.plotka.model.NetworkPeer

class Request(val sender: NetworkPeer, val timestamp: Long) {

  /**
    * Compares requests. Better request is the one that has lower timestamp.
    * If timestamps are equal, node IDs are compared.
    *
    * @param other request that has lower priority if function returns true
    * @return returns true if this request is more prioritized than provided argument
    */
  def isBetterThan(other: Request): Boolean = {
    val otherId = other.sender.id
    val myId = sender.id
    timestamp < other.timestamp || (timestamp == other.timestamp && myId.compareTo(otherId) < 0)
  }
}

object Request {
  def apply(sender: NetworkPeer, timestamp: Long): Request = {
    new Request(sender, timestamp)
  }
}

