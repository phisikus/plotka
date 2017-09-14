/**
  * cluster-builder
  *
  * You can use this script to create a cluster of your services.
  * For given image name containers a docker-compose file will be created.
  * Multiple containers will be defined with separate configuration files.
  * Each configuration file defines certain number of initial peers.
  */

import java.io.{File, PrintWriter}

import scala.io.StdIn

val port = "8080"

/**
  * Gather input data
  */
val imageName = StdIn.readLine("Name of the docker image = ")
val baseContainerName = StdIn.readLine("Base container name = ")
print("Number of containers to create = ")
val totalCount = StdIn.readInt()
print("Number of peers that should each peer be aware of = ")
val knownPeers = StdIn.readInt()
val baseConfigDir = baseContainerName + "/conf"

/**
  * Verify input correctness
  */
assert(totalCount > 0, "Number of containers to create should be > 0")
assert(knownPeers > 0 && knownPeers <= totalCount,
  "Number of known peers should be between 1 and total number of containers")

/**
  * Create base & config directory for storage of the cluster settings
  */
val newDir = new File(baseConfigDir)
assert(newDir.mkdirs(), "Could not create new directory!")

def buildDockerComposeWithConfigs(): Unit = {
  new PrintWriter(baseContainerName + "/docker-compose.yml") {
    write("version: '2'\nservices:\n")

    0.until(totalCount).foreach(i => {
      val containerName = baseContainerName + "-" + i
      write(s"  $containerName:\n")
      write(s"    image: $imageName\n")
      write(s"    volumes:\n")
      write(s"      - ./conf/$containerName:/app/conf\n\n")
      createConfigFile(baseConfigDir, i)
    })
  }.close()
}

def createConfigFile(baseConfigDir: String, i: Int): Unit = {
  val containerConfigDir = baseConfigDir + "/" + baseContainerName + "-" + i
  new File(containerConfigDir).mkdirs()
  new PrintWriter(containerConfigDir + "/app.conf") {

    val currentContainerName = baseContainerName + "-" + i
    write("node {\n  " +
      "id = \"" + currentContainerName + "\"\n  " +
      "port = " + port + "\n  " +
      "address = \"" + currentContainerName + "\"\n  " +
      "peers = [\n" + buildPeersInfo(i) + "\n  ]\n" +
      "}\n")

  }.close()
}

def buildPeersInfo(containerNumber: Int): String = {
  containerNumber.until(containerNumber + knownPeers)
    .map(i => (i + 1) % totalCount)
    .map(i => {
      val peerAddress = baseContainerName + "-" + i
      "    { address = \"" + peerAddress + "\", port = " + port + "}"
    })
    .mkString(",\n")
}

buildDockerComposeWithConfigs()
