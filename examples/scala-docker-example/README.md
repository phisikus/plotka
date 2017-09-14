Scala example with docker
==========================
In this example the node is loading configuration, sends messages to all known nodes and waits until messages are received from those nodes.

It uses plugin for building docker images. To build the image execute:  
```
$ sbt docker
```
The name of created images will be visible in the log.

In this case the configuration should be provided externally. Check `docker-compose.yml` for an example that provides config file using mounted volume. You can run the container by executing:
```
$ docker-compose up
```

You can use the interactive `cluster-builder.scala` script in the _utils_ directory to create configurations and _docker-compose_ file for testing with multiple nodes:
```
$ cd utils
$ scala cluster-builder.scala
Name of the docker image = eu.phisikus/scala-docker-example
Base container name = echo-test
Number of containers to create = 3
Number of peers that should each peer be aware of = 2

$ cd echo-test
$ docker-compose up

Creating network "echotest_default" with the default driver
Creating echotest_echo-test-0_1
Creating echotest_echo-test-2_1
Creating echotest_echo-test-1_1
Attaching to echotest_echo-test-0_1, echotest_echo-test-2_1, echotest_echo-test-1_1
echo-test-0_1  | 20:05:46.512 INFO  e.p.p.c.p.FileConfigurationProvider - Loading default configuration.
echo-test-0_1  | 20:05:46.908 INFO  e.p.p.n.listener.NetworkListener - NetworkListener is waiting for connections: echo-test-0/172.26.0.2:8080
echo-test-0_1  | 20:05:46.973 INFO  EntryPoint - I have received message from NetworkPeer(echo-test-2,echo-test-2,8080) with content = Hello!
echo-test-2_1  | 20:05:46.555 INFO  e.p.p.c.p.FileConfigurationProvider - Loading default configuration.
echo-test-2_1  | 20:05:46.947 INFO  e.p.p.n.listener.NetworkListener - NetworkListener is waiting for connections: echo-test-2/172.26.0.3:8080
echo-test-1_1  | 20:05:47.562 INFO  e.p.p.c.p.FileConfigurationProvider - Loading default configuration.
echo-test-1_1  | 20:05:47.926 INFO  e.p.p.n.listener.NetworkListener - NetworkListener is waiting for connections: echo-test-1/172.26.0.4:8080
echo-test-2_1  | 20:05:47.936 INFO  EntryPoint - I have received message from NetworkPeer(echo-test-0,echo-test-0,8080) with content = Hello!
echo-test-2_1  | 20:05:47.951 INFO  EntryPoint - I have received message from NetworkPeer(echo-test-1,echo-test-1,8080) with content = Hello!
echo-test-1_1  | 20:05:47.946 INFO  EntryPoint - I have received message from NetworkPeer(echo-test-2,echo-test-2,8080) with content = Hello!
echo-test-1_1  | 20:05:47.945 INFO  EntryPoint - I have received message from NetworkPeer(echo-test-0,echo-test-0,8080) with content = Hello!
echo-test-0_1  | 20:05:47.954 INFO  EntryPoint - I have received message from NetworkPeer(echo-test-1,echo-test-1,8080) with content = Hello!
echo-test-2_1  | 20:05:47.960 INFO  EntryPoint - Received replies from: echo-test-0,echo-test-1
echo-test-2_1  | 20:05:47.960 INFO  e.p.p.n.listener.NetworkListener - Stopping the listener...
echo-test-2_1  | 20:05:47.962 INFO  e.p.p.n.listener.NetworkListener - Communication channel closed.
echo-test-1_1  | 20:05:47.967 INFO  EntryPoint - Received replies from: echo-test-2,echo-test-0
echo-test-1_1  | 20:05:47.967 INFO  e.p.p.n.listener.NetworkListener - Stopping the listener...
echo-test-1_1  | 20:05:47.967 INFO  e.p.p.n.listener.NetworkListener - Communication channel closed.
echo-test-0_1  | 20:05:48.055 INFO  EntryPoint - Received replies from: echo-test-2,echo-test-1
echo-test-0_1  | 20:05:48.055 INFO  e.p.p.n.listener.NetworkListener - Stopping the listener...
echo-test-0_1  | 20:05:48.056 INFO  e.p.p.n.listener.NetworkListener - Communication channel closed.
echotest_echo-test-2_1 exited with code 0
echotest_echo-test-1_1 exited with code 0
echotest_echo-test-0_1 exited with code 0

```

