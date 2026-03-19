package com.nps.resource;

import com.nps.entity.SortingRule;
import com.nps.rag.EmbeddingService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SortingRuleResource {

    @Inject
    EmbeddingService embeddingService;

    @GET
    public List<SortingRule> list() {
        return SortingRule.listAll();
    }

    @POST
    @Transactional
    public Response create(SortingRule rule) {
        rule.persist();
        embeddingService.reindexRules();
        return Response.status(201).entity(rule).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public SortingRule update(@PathParam("id") Long id, SortingRule update) {
        SortingRule rule = SortingRule.findById(id);
        if (rule == null) throw new NotFoundException();
        rule.description = update.description;
        rule.postalCodePattern = update.postalCodePattern;
        rule.assignedBelt = update.assignedBelt;
        rule.priority = update.priority;
        rule.active = update.active;
        embeddingService.reindexRules();
        return rule;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        SortingRule.deleteById(id);
        embeddingService.reindexRules();
        return Response.noContent().build();
    }

    @POST
    @Path("/reindex")
    public Response reindex() {
        embeddingService.reindexRules();
        return Response.ok().build();
    }
}
