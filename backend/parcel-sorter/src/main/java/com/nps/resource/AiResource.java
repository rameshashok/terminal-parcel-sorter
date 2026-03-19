package com.nps.resource;

import com.nps.rag.RagService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/ai")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiResource {

    @Inject
    RagService ragService;

    @POST
    @Path("/ask")
    public AiResponse ask(AiRequest request) {
        return new AiResponse(ragService.askWithContext(request.question()));
    }

    public record AiRequest(String question) {}
    public record AiResponse(String answer) {}
}
