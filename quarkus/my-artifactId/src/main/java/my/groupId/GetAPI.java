package my.groupId;

import javax.jms.JMSException;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Sample API to put a message on the queue

@Path("/get")
public class GetAPI {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() throws JMSException {
    return Jms.get();
    }
}
