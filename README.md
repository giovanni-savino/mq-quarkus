# Quarkus + IBM MQ JMS sample
This sample code shows a sample use of Quarkus on *traditional JVM* and JMS 2.0 to send MQ messages

If you want to use Quarkus native mode (GraalVM), please refers to [this](https://github.com/ibm-messaging/mq-dev-patterns/tree/master/amqp-qpid) tutorial

This repo was configured by starting a basic Quarkus environment with the following command:
```console
./mvnw quarkus:dev
```
and then importing the standard JMS classese of [this](https://github.com/ibm-messaging/mq-dev-patterns/tree/master/JMS) repo


## Prerequisite
* An IBM MQ deployed and configured
* A local environment with:
  * Maven CLI installed
  * A JVM (eg JDK 20.x)

## Run the local demo environment

1. Clone the following repo and go to the quarkys/my-artifactId directory

2. Configure the MQ endpoint -> at the moment editing the static variables in the src/java/my/groupId/ContectCreation.java file -> Next realase edit from env variables
The current static parameters are:

3. Run the mvn project
```console
./mvnw quarkus:dev
```

3. To simulate a put message, you can open a browser on the dev enviroment on the /put API

```console
http://localhost:8080/put
```
or start a put message with a curl
```console
curl http://localhost:8080/put
```
Get message from the queue are displayed in log terminal


