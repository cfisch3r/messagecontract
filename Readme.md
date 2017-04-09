# Introduction

This sample code shows how to setup an integration test for
RabbitMQ clients using Docker.

# Prerequisites

A DOCKER_HOST environment variable has to be set. On Mac this is:
unix:///var/run/docker.sock

After a while, this stops working on my Mac. Than I stop docker, remove the /var/run/docker.sock file and restart docker.
This fixes the problem for me.

# Use Case Scenario

The use case is quite simple: The adapter should consume messages from one queue.
If there are any problems the message is rejected and forwarded to an error queue
using the [Dead Letter Exchange feature](https://www.rabbitmq.com/dlx.html) of RabbitMQ.

# How it works

The RabbitMQ server is started as Docker container using the Junit rule from 
[geowarin](https://github.com/geowarin/docker-junit-rule). 
All the setup of the RabbitMQ configuration like queues and exchanges are done in the 
[MessageBrokerDouble](src/test/java/de/agiledojo/messagecontract/MessageBrokerDouble.java).

