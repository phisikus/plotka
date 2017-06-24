package pl.weimaraner.plotka.model

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class SessionState {
  val sessionProperties: mutable.Map[String, String] = new TrieMap[String, String]()

  def getProperty(key: String): Option[String] = {
    sessionProperties.get(key)
  }

  def setProperty(key: String, value: String): Unit = {
    sessionProperties.put(key, value)
  }


}
