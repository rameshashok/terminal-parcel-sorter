package com.nps.kafka;

import com.nps.entity.Parcel;
import com.nps.service.SortingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SortingConsumer {

    private static final Logger LOG = Logger.getLogger(SortingConsumer.class);

    @Inject
    SortingService sortingService;

    @Incoming("parcel-events-in")
    @Transactional
    public void process(String eventJson) {
        try {
            LOG.infof("Processing sorting event: %s", eventJson);
            sortingService.processPendingSorting();
        } catch (Exception e) {
            LOG.errorf("Error processing event: %s", e.getMessage());
        }
    }
}
