package com.nps.resource;

import com.nps.entity.Parcel;
import com.nps.rag.AssistService;
import com.nps.service.SortingService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/api/parcels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParcelResource {

    @Inject
    SortingService sortingService;

    @Inject
    AssistService assistService;

    @GET
    public List<Parcel> list() {
        return Parcel.listAll();
    }

    @GET
    @Path("/{id}")
    public Parcel get(@PathParam("id") Long id) {
        return Parcel.findById(id);
    }

    @POST
    public Response create(Parcel parcel) {
        return Response.status(201).entity(sortingService.registerParcel(parcel)).build();
    }

    @POST
    @Path("/{id}/sort")
    @Transactional
    public Parcel sort(@PathParam("id") Long id) {
        Parcel parcel = Parcel.findById(id);
        if (parcel == null) throw new NotFoundException();
        return sortingService.sortParcel(parcel);
    }

    @PUT
    @Path("/{id}/status")
    @Transactional
    public Parcel updateStatus(@PathParam("id") Long id, Parcel update) {
        Parcel parcel = Parcel.findById(id);
        if (parcel == null) throw new NotFoundException();
        parcel.status = update.status;
        return parcel;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Parcel.deleteById(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/batch")
    public List<BatchResult> uploadBatch(List<Parcel> parcels) {
        List<BatchResult> results = new ArrayList<>();
        for (int i = 0; i < parcels.size(); i++) {
            Parcel p = parcels.get(i);
            String validationError = validateRow(p);
            if (validationError != null) {
                String context = String.format("trackingNumber=\"%s\", origin=\"%s\", destination=\"%s\", postalCode=\"%s\", weightKg=\"%s\"",
                    p.trackingNumber, p.origin, p.destination, p.postalCode, p.weightKg);
                String guidance = assistService.assist("batch_upload", validationError, p.trackingNumber != null ? p.trackingNumber : "", context);
                results.add(new BatchResult(i + 1, p.trackingNumber, "ERROR", validationError, guidance));
            } else {
                try {
                    sortingService.registerParcel(p);
                    results.add(new BatchResult(i + 1, p.trackingNumber, "OK", null, null));
                } catch (Exception e) {
                    results.add(new BatchResult(i + 1, p.trackingNumber, "ERROR", e.getMessage(), null));
                }
            }
        }
        return results;
    }

    private String validateRow(Parcel p) {
        if (p.trackingNumber == null || p.trackingNumber.isBlank()) return "required:trackingNumber";
        if (!p.trackingNumber.matches("[A-Za-z0-9\\-]{6,20}")) return "invalid_format:trackingNumber";
        if (p.origin == null || p.origin.isBlank()) return "required:origin";
        if (p.destination == null || p.destination.isBlank()) return "required:destination";
        if (p.postalCode == null || p.postalCode.isBlank()) return "required:postalCode";
        if (!p.postalCode.matches("[1-6].*")) return "invalid_format:postalCode";
        if (p.weightKg <= 0.1) return "min:weightKg";
        if (p.weightKg > 70) return "max:weightKg";
        return null;
    }

    public record BatchResult(int row, String trackingNumber, String status, String error, String aiGuidance) {}
}
