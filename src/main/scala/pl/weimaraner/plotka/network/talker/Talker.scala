package pl.weimaraner.plotka.network.talker

import pl.weimaraner.plotka.model.NetworkPeer

trait Talker {
  def send(recipient: NetworkPeer, messageBody: Serializable)
}
