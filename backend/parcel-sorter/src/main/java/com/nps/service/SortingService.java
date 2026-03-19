package com.nps.service;

import com.nps.entity.Parcel;
import com.nps.entity.ParcelStatus;
import com.nps.entity.SortingRule;
import com.nps.kafka.ParcelEventProducer;
import com.nps.rag.RagService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SortingService {

    @Inject
    RagService ragService;

    @Inject
    ParcelEventProducer producer;

    @Transactional
    public Parcel registerParcel(Parcel parcel) {
        parcel.status = ParcelStatus.RECEIVED;
        parcel.persist();
        producer.sendParcelEvent(parcel);
        return parcel;
    }

    @Transactional
    public void processPendingSorting() {
        List<Parcel> parcels = Parcel.list("status", ParcelStatus.RECEIVED);
        parcels.forEach(this::sortParcel);
    }

    @Transactional
    public Parcel sortParcel(Parcel parcel) {
        parcel.status = ParcelStatus.SORTING;

        SortingRule rule = SortingRule.findByPostalCode(parcel.postalCode);
        if (rule != null) {
            parcel.assignedBelt = rule.assignedBelt;
            parcel.aiReasoning = "Rule-based: " + rule.description;
        } else {
            String aiResponse = ragService.decideSortingBelt(parcel.postalCode, parcel.weightKg, parcel.destination);
            parcel.assignedBelt = extractBelt(aiResponse);
            parcel.aiReasoning = aiResponse;
        }

        parcel.status = ParcelStatus.SORTED;
        parcel.persist();
        producer.sendParcelEvent(parcel);
        return parcel;
    }

    private String extractBelt(String aiResponse) {
        if (aiResponse.contains("Belt")) {
            int idx = aiResponse.indexOf("Belt");
            return aiResponse.substring(idx, Math.min(idx + 8, aiResponse.length())).trim();
        }
        return "Belt-A";
    }
}
