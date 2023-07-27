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
Get message from the queue are displayed in log terminal


# APPENDIX

## Run a local MQ dev container 
If you wante to use a local MQ container, it is possible to use a basic [MQ dev image](https://developer.ibm.com/tutorials/mq-connect-app-queue-manager-containers/) with podman or docker
```console
 docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --publish 1414:1414 --publish 9443:9443 --detach --env MQ_APP_PASSWORD=passw0rd --name QM1 icr.io/ibm-messaging/mq:latest
```
If you want to access to the console you can open the browser on the url and access with *admin* user
```console
https://localhost:9443/ibmmq/console/login.html
```