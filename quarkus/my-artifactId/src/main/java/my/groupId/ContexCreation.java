
package my.groupId;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import javax.jms.Destination;
// import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
// import javax.jms.Message;
// import javax.jms.TextMessage;
// import javax.jms.JMSRuntimeException;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
// import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

// import com.ibm.mq.constants.MQConstants;
// import com.ibm.mq.MQException;

public class ContexCreation {

    private static final Logger logger = Logger.getLogger("com.ibm.mq.samples.jms");

    // Create variables for the connection to MQ
    private static String HOST= "localhost";
    private static int PORT= 1414;
    private static String CHANNEL= "DEV.APP.SVRCONN"; // Channel name
    private static String QMGR="QM1"; //System.getenv("QMGR"); // Queue manager name
    private static String APP_USER="app"; // User name that application uses to connect to MQ
    private static String APP_PASSWORD="passw0rd"; // Password that the application uses to connect to MQ
    private static String QUEUE_NAME="DEV.QUEUE.1"; // Queue that the application uses to put and get messages
                                      // to and from
    private static String CIPHER_SUITE;
    private static String CCDTURL;
    private static Boolean BINDINGS = false;

    
    private static JMSContext context;
    private static Destination destination;


    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    void onStart(@Observes StartupEvent ev) {
        

        JmsConnectionFactory connectionFactory = createJMSConnectionFactory();
        setJMSProperties(connectionFactory);
        logger.info("created connection factory");

        context = connectionFactory.createContext();

        logger.info("context created");
        destination = context.createQueue("queue:///" + QUEUE_NAME);
        logger.info("destination created");

        
        // scheduler.submit(this);
    }

    public static Destination getDestination() {
        return destination;
    }

    public static JMSContext getContext() {
        return context;
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
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
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsGet (JMS)");
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
