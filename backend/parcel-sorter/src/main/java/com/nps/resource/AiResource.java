package com.nps.resource;

import com.nps.rag.AssistService;
import com.nps.rag.RagService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api/ai")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiResource {

    private static final Logger LOG = Logger.getLogger(AiResource.class);

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

    @POST
    @Path("/feedback")
    public Response feedback(FeedbackRequest request) {
        LOG.infof("[TELEMETRY] field='%s' errorCode='%s' corrected=%b",
            request.field(), request.errorCode(), request.corrected());
        return Response.noContent().build();
    }

    public record AiRequest(String question) {}
    public record AiResponse(String answer) {}
    public record AssistRequest(String field, String errorCode, String value, String formContext) {}
    public record AssistResponse(String guidance) {}
    public record FeedbackRequest(String field, String errorCode, boolean corrected) {}
}
