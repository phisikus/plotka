# plotka [![Build Status](https://travis-ci.org/phisikus/plotka.svg?branch=master)](https://travis-ci.org/phisikus/plotka)

Simple message-passing library written in Scala. It uses non-blocking TCP/IP channels (NIO) and provides functional, event-driven way of writing applications.

The library distinguishes _Talker_ and _Listener_ as separate instances that can respectively send messages to a _Peer_ and handle incomming ones by invoking provided message consumer. Alternatively a _StateMachine_ instance can be prepared and binded with _Listener_ so each time a message is recevied, event can be emitted which can cause change of the state (check _StateMachineNetworkMessageConsumer_).

In general the library is a work in progress - check out the _examples_ directory for usage inspiration:
- _java-example_ - Simple usage example in Java that sends and receives a single message.
- _scala-docker-example_ - More complex example in Scala where each node sends messages to all known nodes and finishes when the responses are received.
- _ricart-agrawala_ - Example mutual exclusion algorithm implementation in Scala (experimental)
