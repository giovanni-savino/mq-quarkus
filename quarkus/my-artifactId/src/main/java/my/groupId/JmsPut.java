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
import javax.jms.TextMessage;
import com.ibm.msg.client.wmq.WMQConstants;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;

import com.ibm.mq.jms.MQDestination;



public class JmsPut {

    private static final Level LOGLEVEL = Level.ALL;
    private static final Logger logger = Logger.getLogger("com.ibm.mq.samples.jms");


    private static JMSContext context = null;
    private static Destination destination = null;
    private static JMSProducer producer = null;
    
    public static String put() throws JMSException {
         

        Random R = new Random();
        TextMessage message = context.createTextMessage("This is a sample message "+R.nextInt() );
        producer.send(destination, message);

        logger.info("Sent message "+ message.getText());
        return message.getText();
    }
    
    void onStart(@Observes StartupEvent ev) {
         initialiseLogging();
        logger.info("Put application is starting");

        context = ContexCreation.getContext();
        destination = ContexCreation.getDestination();
        setTargetClient(destination);

        producer = context.createProducer();
        logger.info("producer created");
    }

    private static void setTargetClient(Destination destination) {
      try {
          MQDestination mqDestination = (MQDestination) destination;
          mqDestination.setTargetClient(WMQConstants.WMQ_CLIENT_NONJMS_MQ);
      } catch (JMSException jmsex) {
        logger.warning("Unable to set target destination to non JMS");
      }
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
