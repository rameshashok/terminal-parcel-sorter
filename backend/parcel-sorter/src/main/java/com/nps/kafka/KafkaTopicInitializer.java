package com.nps.kafka;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class KafkaTopicInitializer {

    private static final Logger LOG = Logger.getLogger(KafkaTopicInitializer.class);

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    void onStart(@Observes StartupEvent ev) {
        try (AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers))) {

            Set<String> existing = admin.listTopics().names().get();
            if (!existing.contains("parcel-events")) {
                admin.createTopics(List.of(new NewTopic("parcel-events", 1, (short) 1))).all().get();
                LOG.info("Created Kafka topic: parcel-events");
            }
        } catch (Exception e) {
            LOG.warnf("Could not pre-create Kafka topic (will be auto-created): %s", e.getMessage());
        }
    }
}
