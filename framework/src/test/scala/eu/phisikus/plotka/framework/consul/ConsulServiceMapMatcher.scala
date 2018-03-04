package eu.phisikus.plotka.framework.consul

import com.orbitz.consul.model.health.Service
import eu.phisikus.plotka.conf.NodeConfiguration
import org.scalatest.matchers.{MatchResult, Matcher}

class ConsulServiceMapMatcher(serviceName: String, nodeConfiguration: NodeConfiguration) extends Matcher[java.util.Map[String, Service]] {

  override def apply(left: java.util.Map[String, Service]): MatchResult = {
    MatchResult(
      left.containsKey(nodeConfiguration.id) &&
        checkServiceFields(left.get(nodeConfiguration.id)),
      s"""Service map did not contain information about service $serviceName""",
      s"""Service map contained information about service $serviceName"""
    )
  }

  private def checkServiceFields(service: Service): Boolean = {
    service.getAddress == nodeConfiguration.address &&
      service.getId == nodeConfiguration.id &&
      service.getPort == nodeConfiguration.port &&
      service.getService == serviceName
  }

}

object ConsulServiceMapMatcher {
  def containsService(serviceName: String,
                      nodeConfiguration: NodeConfiguration): Matcher[java.util.Map[String, Service]] = {
    new ConsulServiceMapMatcher(serviceName, nodeConfiguration)
  }
}


