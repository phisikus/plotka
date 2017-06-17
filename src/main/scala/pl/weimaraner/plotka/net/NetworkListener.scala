package pl.weimaraner.plotka.net

import java.net.ServerSocket
import java.util.UUID

import pl.weimaraner.plotka.model.NetworkPeer

class NetworkListener(port: Int) {
  require(port >= 0 && port <= 65536)
  val serverSocket = new ServerSocket(port)
  val serverAddress = "0.0.0.0"
  val serverPeer = NetworkPeer(UUID.fromString(serverAddress + ":" + port).toString, serverAddress, port)
}
