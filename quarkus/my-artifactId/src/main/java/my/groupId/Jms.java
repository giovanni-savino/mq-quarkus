/*
* (c) Copyright IBM Corporation 2019, 2023
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package my.groupId;

import java.util.Random;
import java.util.logging.*;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSConsumer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;

import com.ibm.mq.MQException;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQDestination;

//import com.ibm.mq.jms.MQConnectionFactory;

public class Jms {

    private static final Level LOGLEVEL = Level.ALL;
    private static final Logger logger = Logger.getLogger("com.ibm.mq.samples.jms");
    private static long TIMEOUTTIME = 2000;  // 2 Seconds

   
      // Create variables for the connection to MQ
      private static String HOST;// "localhost"
      private static int PORT; //1414;
      private static String CHANNEL;// "DEV.APP.SVRCONN"  Channel name
      private static String QMGR;//"QM1"  Queue manager name
      private static String APP_USER;// "app"  User name that application uses to connect to MQ
      private static String APP_PASSWORD;// "passw0rd"  // Password that the application uses to connect to MQ
      private static String QUEUE_NAME;// "DEV.QUEUE.1" // Queue that the application uses to put and get messages
                                        // to and from
      private static String CIPHER_SUITE;
      private static String CCDTURL;
      private static Boolean BINDINGS = false;
      private static JMSContext context = null;
      private static Destination destination = null;
      private static JMSProducer producer = null;
      private static JMSConsumer consumer = null;
  

    void onStart(@Observes StartupEvent ev) {
        initialiseLogging();
        setEnvVar();
        logger.info("Put application is starting");

        JmsConnectionFactory connectionFactory = createJMSConnectionFactory();
        setJMSProperties(connectionFactory);
        logger.info("created connection factory");

        context = connectionFactory.createContext();
        logger.info("context created");

        // Set targetClient to be non JMS, so no JMS headers are transmitted.
        // Only one of these settings is required, but both shown here.
        // 1. Add targetClient parameter to Queue uri
        destination = context.createQueue("queue:///" + QUEUE_NAME + "?targetClient=1");
        // destination = context.createQueue("queue:///" + QUEUE_NAME);
        logger.info("destination created");

        // 2. Cast destination queue to underlying MQQueue and set target client
        setTargetClient(destination);

        producer = context.createProducer();
        logger.info("producer created");

        consumer = context.createConsumer(destination);
        logger.info("consumer created");


    }


    
    public static String put() throws JMSException {

        Random R = new Random();
        TextMessage message = context.createTextMessage("This is a sample message "+R.nextInt() );
        producer.send(destination, message);

        logger.info("Sent message "+ message.getText());
        return message.getText()+"\n";
    }

    public static String get() throws JMSException {
        String Message="";
          
            try {
                Message=retrieveFromEndpoint();
            } catch (JMSRuntimeException ex) {
                if (! canContinue(ex)) {
                }
            }
            return Message;
        }


    
    
    private static boolean canContinue(JMSRuntimeException ex) {
        if (null != ex.getCause() && ex.getCause() instanceof MQException) {
            MQException innerException = (MQException) ex.getCause();

            if (MQConstants.MQRC_HOST_NOT_AVAILABLE == innerException.getReason()) {
                logger.info("Host not available, skipping message gets from this host");
                return true;
            }
        }

        logger.warning("Unexpected exception will be terminating process");
        recordFailure(ex);
        return false;
    }

    private static String retrieveFromEndpoint() throws JMSException {
        boolean continueProcessing = true;
        String Messages="";
        while (continueProcessing) {
            try {
                Message receivedMessage = consumer.receive(TIMEOUTTIME);

                if (receivedMessage == null && Messages.isEmpty()){
                    logger.info("No message on the queue");
                    continueProcessing = false;
                    return ("No message on the queue");
                } else if (receivedMessage == null && !Messages.isEmpty()) {
                  logger.info("All messages are read");
                  return Messages;
                } else {
                  logger.info("Message received");
                  Messages += getAndDisplayMessageBody(receivedMessage)+"\n";
                }
            } catch (JMSRuntimeException jmsex) {
                jmsex.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
      return Messages;
    }

    private static String getAndDisplayMessageBody(Message receivedMessage) {
        if (receivedMessage instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) receivedMessage;
            try {
                return textMessage.getText();
            } catch (JMSException jmsex) {
                recordFailure(jmsex);
            }
        } else if (receivedMessage instanceof Message) {
            return ("Message received was not of type TextMessage.\n");
        } else {
            return ("Received object not of JMS Message type!\n");
        }
        return "No received message";
    }
    

    public static void setEnvVar(){
        HOST = checkEnv("HOST","localhost");
        PORT = Integer.parseInt(checkEnv("PORT","1414"));
        CHANNEL = checkEnv("CHANNEL","DEV.APP.SVRCONN");
        QMGR = checkEnv("QMGR","QM1");
        APP_USER = checkEnv("APP_USER","app");
        APP_PASSWORD = checkEnv("APP_PASSWORD","passw0rd");
        QUEUE_NAME = checkEnv("QUEUE_NAME","DEV.QUEUE.1");
  }

  public static String checkEnv(String EnvVar, String DefaultVar){
        String ReturnedVar;
        if (System.getenv(EnvVar) == null){
          ReturnedVar=DefaultVar;
         } else {
              ReturnedVar= System.getenv(EnvVar);
          }
        return ReturnedVar;
  }

    private static JmsConnectionFactory createJMSConnectionFactory() {
        JmsFactoryFactory ff;
        JmsConnectionFactory cf;
        try {
            ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            cf = ff.createConnectionFactory();
        } catch (JMSException jmsex) {
            recordFailure(jmsex);
            cf = null;
        }
        return cf;
    }

    private static void setJMSProperties(JmsConnectionFactory cf) {
        try {
            if (null == CCDTURL) {
                cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
                cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
                if (null == CHANNEL && !BINDINGS) {
                    logger.warning("When running in client mode, either channel or CCDT must be provided");
                } else if (null != CHANNEL) {
                    cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
                }
            } else {
                logger.info("Will be making use of CCDT File " + CCDTURL);
                cf.setStringProperty(WMQConstants.WMQ_CCDTURL, CCDTURL);
            }

            if (BINDINGS) {
                cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
            } else {
                cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            }

            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPut (JMS)");
            if (null != APP_USER && !APP_USER.trim().isEmpty()) {
                cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
                cf.setStringProperty(WMQConstants.USERID, APP_USER);
                cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
            }
            if (CIPHER_SUITE != null && !CIPHER_SUITE.isEmpty()) {
                cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, CIPHER_SUITE);
            }
        } catch (JMSException jmsex) {
            recordFailure(jmsex);
        }
        return;
    }

    private static void setTargetClient(Destination destination) {
      try {
          MQDestination mqDestination = (MQDestination) destination;
          mqDestination.setTargetClient(WMQConstants.WMQ_CLIENT_NONJMS_MQ);
      } catch (JMSException jmsex) {
        logger.warning("Unable to set target destination to non JMS");
      }
    }

    private static void recordFailure(Exception ex) {
        if (ex != null) {
            if (ex instanceof JMSException) {
                processJMSException((JMSException) ex);
            } else {
                logger.warning(ex.getMessage());
            }
        }
        logger.warning("FAILURE");
        return;
    }

    private static void processJMSException(JMSException jmsex) {
        logger.warning(jmsex.getMessage());
        Throwable innerException = jmsex.getLinkedException();
        if (innerException != null) {
            logger.warning("Inner exception(s):");
        }
        while (innerException != null) {
            logger.warning(innerException.getMessage());
            innerException = innerException.getCause();
        }
        return;
    }

    private static void initialiseLogging() {
        Logger defaultLogger = Logger.getLogger("");
        Handler[] handlers = defaultLogger.getHandlers();
        if (handlers != null && handlers.length > 0) {
            defaultLogger.removeHandler(handlers[0]);
        }

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(LOGLEVEL);
        logger.addHandler(consoleHandler);

        logger.setLevel(LOGLEVEL);
        logger.finest("Logging initialised");
    }

}
