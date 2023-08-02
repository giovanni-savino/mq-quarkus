# Quarkus + IBM MQ JMS sample
This sample code shows a sample use of Quarkus on *traditional JVM* and JMS 2.0 to send MQ messages

If you want to use Quarkus native mode (GraalVM), please refers to [this](https://github.com/ibm-messaging/mq-dev-patterns/tree/master/amqp-qpid) tutorial

This repo was configured by starting a basic Quarkus environment with the following command:
```console
mvn io.quarkus.platform:quarkus-maven-plugin:3.2.2.Final:create \
    -DprojectGroupId=my-groupId \
    -DprojectArtifactId=my-artifactId
```
and then importing the standard JMS classese of [this](https://github.com/ibm-messaging/mq-dev-patterns/tree/master/JMS) repo

We edited the pom.xml file and added the following dependency:
```console
<dependency>
        <groupId>javax.jms</groupId>
        <artifactId>javax.jms-api</artifactId>
        <version>2.0.1</version>
</dependency>
<dependency>
        <groupId>com.ibm.mq</groupId>
        <artifactId>com.ibm.mq.allclient</artifactId>
        <version>9.3.3.0</version>
</dependency>
```

## Prerequisite
* An IBM MQ deployed and configured
* A local environment with:
  * Maven CLI installed
  * A JVM (eg JDK 20.x)

## Run the local demo environment

1. Clone the following repo and go to the quarkys/my-artifactId directory
```console
git clone https://github.com/giovanni-savino/mq-quarkus.git
cd mq-quarkus/quarkus/my-artifactId/
```
2. Configure the MQ endpoint -> at the moment editing the static variables are in the [src/java/my/groupId/ContectCreation.java](quarkus/my-artifactId/src/main/java/my/groupId/ContexCreation.java) file
The current static parameters are:
```console
HOST= "localhost"
PORT= 1414
CHANNEL= "DEV.APP.SVRCONN"
QMGR="QM1"
APP_USER="app"
APP_PASSWORD="passw0rd"
QUEUE_NAME="DEV.QUEUE.1"
```
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

To get all the messages on the queue you can run:
```console
curl http://localhost:8080/get
```

# APPENDIX

## Run a local MQ dev container 
If you wante to use a local MQ container, it is possible to use a basic [MQ dev image](https://developer.ibm.com/tutorials/mq-connect-app-queue-manager-containers/) with podman or docker
```console
 docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --publish 1414:1414 --publish 9443:9443 --detach --env MQ_APP_PASSWORD=passw0rd --name QM1 icr.io/ibm-messaging/mq:latest
```
To access the console open the browser on the url with *admin* user
```console
https://localhost:9443/ibmmq/console/login.html
```

## Run a local MQ + MQ source&Sink + KafkaConnect + Strimzi
If you want to test an End 2 End scenario on your local laptop where a message is:
* Sent message to a MQ queue (DEV.QUEUE.1)
* Use MQ source to write to a Kafka topic (mq-source)
* Use MQ sink to read from the Kafka topic and write a on a different queue (DEV.QUEUE.2) 

You can use the /local docker-compose file to deploy all the components

To understand how to build and use the local MQ source connector, please refers to [this](https://github.com/ibm-messaging/kafka-connect-mq-source/blob/master/UsingMQwithKafkaConnect.md) repo

To run the local test environment, go to the /local directory and run 
```console
cd /local
docker-compose up -d
```
A set of 6 containers are started:
* A Kafka broker
* A kafka zookeeper
* An MQ broker
* An MQ source connector
* An MQ sink connector
* A SampleMQQuarkusApp container

To configure the MQ source and Sink connectors you need to run the commands
```console
curl -X POST -H "Content-Type: application/json" http://localhost:8083/connectors \
  --data @mq-sink.json

curl -X POST -H "Content-Type: application/json" http://localhost:8083/connectors \
  --data @mq-source.json

```console
To start the scnario you need to open a terminal to see messages on the kafka topic 
```console
 docker  exec -it $(docker ps | grep kafka-ser | awk '{print $1}') bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic mq-source
```

On another terminal run the command to put messages on the first queue:
```console
curl http://localhost:8080/put
```
To see the messages on the last step you can run open the browser to see the message on the MQ queue DEV.QUEUE.2

```console
https://localhost:9443/ibmmq/console/#/manage/qmgr/QM1/queue/local/DEV.QUEUE.2/view
```


## Run a remote MQ + MQ source&Sink + KafkaConnect + Strimzi
If you want to test an End 2 End scenario on a remote OCP where a message is:
* Sent to a MQ queue
* Use MQ source to write to a Kafka topic
* Use MQ sink to read from the Kafkat topic and write a on a different queue you can use the /OCP files to deploy all the components