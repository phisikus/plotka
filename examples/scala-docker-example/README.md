Scala example with docker
==========================
This example uses plugin for building docker images. To build the image execute:  
```
sbt docker
```
The name of created images will be visible in the log.

In this case the configuration should be provided externally. Check `docker-compose.yml` for an example that provides config file using mounted volume. You can run the container by executing:
```
docker-compose up
```