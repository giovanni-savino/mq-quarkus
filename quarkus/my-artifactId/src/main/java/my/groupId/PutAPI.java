package my.groupId;

import javax.jms.JMSException;

import java.util.logging.*;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Sample API to put a message on the queue

@Path("/put")
public class PutAPI {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String put() throws JMSException {
    return JmsPut.put();
    }
}
