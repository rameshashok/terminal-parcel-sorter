package com.nps.resource;

import com.nps.entity.Parcel;
import com.nps.service.SortingService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/parcels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParcelResource {

    @Inject
    SortingService sortingService;

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
}
