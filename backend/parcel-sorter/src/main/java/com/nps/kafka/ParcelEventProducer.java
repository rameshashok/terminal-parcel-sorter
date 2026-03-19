package com.nps.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nps.entity.Parcel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class ParcelEventProducer {

    @Inject
    @Channel("parcel-events-out")
    Emitter<String> emitter;

    @Inject
    ObjectMapper mapper;

    public void sendParcelEvent(Parcel parcel) {
        try {
            emitter.send(mapper.writeValueAsString(new ParcelEvent(parcel.id, parcel.trackingNumber, parcel.status.name())));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send parcel event", e);
        }
    }

    public record ParcelEvent(Long id, String trackingNumber, String status) {}
}
