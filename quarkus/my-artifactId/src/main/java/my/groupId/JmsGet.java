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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSRuntimeException;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.MQException;

@ApplicationScoped
public class JmsGet implements Runnable{

    private static final Logger logger = Logger.getLogger("com.ibm.mq.samples.jms");

    private static long TIMEOUTTIME = 5000;  // 5 Seconds
    
    private static JMSContext context = ContexCreation.getContext();

    private static Destination destination = ContexCreation.getDestination();
    private static JMSConsumer consumer;
    private static boolean continueProcessing = true;

    @Override
    public void run() {
        logger.info("Get application is starting");
       
            try {
                retrieveFromEndpoint();
            } catch (JMSRuntimeException ex) {
                if (! canContinue(ex)) {
                }
            }
        }
    
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();


    void onStart(@Observes StartupEvent ev) {
        
        context = ContexCreation.getContext();
        destination = ContexCreation.getDestination();
        consumer = context.createConsumer(destination);
        logger.info("consumer created");
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
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

    private static void retrieveFromEndpoint() {
   

        while (continueProcessing) {
            try {
                Message receivedMessage = consumer.receive(TIMEOUTTIME);

                if (receivedMessage == null) {
                    //logger.info("No message received from this endpoint");
                     //continueProcessing = false;
                } else {
                  getAndDisplayMessageBody(receivedMessage);
                }
            } catch (JMSRuntimeException jmsex) {
                jmsex.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static void getAndDisplayMessageBody(Message receivedMessage) {
        if (receivedMessage instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) receivedMessage;
            try {
                logger.info("Received message: " + textMessage.getText());
            } catch (JMSException jmsex) {
                recordFailure(jmsex);
            }
        } else if (receivedMessage instanceof Message) {
            logger.info("Message received was not of type TextMessage.\n");
        } else {
            logger.info("Received object not of JMS Message type!\n");
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
        System.out.println("FAILURE");
        return;
    }

    private static void processJMSException(JMSException jmsex) {
        logger.info(jmsex.getMessage());
        Throwable innerException = jmsex.getLinkedException();
        logger.info("Exception is: " + jmsex);
        if (innerException != null) {
            logger.info("Inner exception(s):");
        }
        while (innerException != null) {
            logger.warning(innerException.getMessage());
            innerException = innerException.getCause();
        }
        return;
    }

}
