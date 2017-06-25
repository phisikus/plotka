package pl.weimaraner.plotka.model

import scala.collection.concurrent.TrieMap

/**
  * The purpose of this class is to provide a way to store state of computation between
  * executions of handlers for events like incoming messages retrieval.
  */
class SessionState {
  val sessionProperties: TrieMap[String, AnyRef] = new TrieMap[String, AnyRef]()

  def getProperty(key: String): Option[AnyRef] = {
    sessionProperties.get(key)
  }

  def setProperty(key: String, value: AnyRef): Unit = {
    sessionProperties.put(key, value)
  }

  def compareAndSwap(key: String, oldValue: AnyRef, newValue: AnyRef): Boolean = {
    sessionProperties.replace(key, oldValue, newValue)
  }

}
