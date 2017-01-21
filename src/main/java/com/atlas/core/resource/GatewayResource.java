package com.atlas.core.resource;

import com.atlas.core.MessageProducer;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/gateway")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatewayResource {

    private final MessageProducer<Map<String, String>> messageProducer;

    public GatewayResource(MessageProducer<Map<String, String>> messageProducer) {
        this.messageProducer = messageProducer;
    }

    @POST
    @Path("/{type}")
    public Response send(@PathParam("type") String type, Map<String, String> payload) {
        messageProducer.send(payload, type.toUpperCase());
        return Response.ok("Sent to destination").build();
    }

}
