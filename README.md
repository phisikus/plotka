# plotka

Simple message-passing library written in Scala. It uses non-blocking TCP/IP channels (NIO) and provides functional, event-driven way of writing applications.

The library distinguishes _Talker_ and _Listener_ as separate instances that can respectively send messages to a _Peer_ and handle incomming ones by invoking provided message consumer.

It is a work in progress - check out the _java-example_ project for reference.
 
```xml
<repositories>
         <repository>
             <id>phisikus-repo</id>
             <name>Phisikus' Maven Repository</name>
             <url>http://phisikus.eu/maven2</url>
         </repository>
</repositories>
<dependencies>
         <dependency>
             <groupId>eu.phisikus</groupId>
             <artifactId>plotka_2.12</artifactId>
             <version>0.0.1</version>
         </dependency>
</dependencies>
```


