package com.nps.resource;

import com.nps.rag.AssistService;
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

    @Inject
    AssistService assistService;

    @POST
    @Path("/ask")
    public AiResponse ask(AiRequest request) {
        return new AiResponse(ragService.askWithContext(request.question()));
    }

    @POST
    @Path("/assist")
    public AssistResponse assist(AssistRequest request) {
        String guidance = assistService.assist(
            request.field(), request.errorCode(), request.value(), request.formContext());
        return new AssistResponse(guidance);
    }

    public record AiRequest(String question) {}
    public record AiResponse(String answer) {}
    public record AssistRequest(String field, String errorCode, String value, String formContext) {}
    public record AssistResponse(String guidance) {}
}
