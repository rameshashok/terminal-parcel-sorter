package com.nps.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class KnowledgeBaseService {

    private static final Logger LOG = Logger.getLogger(KnowledgeBaseService.class);

    private static final List<String> KNOWLEDGE = List.of(
        "Tracking Number field: A unique parcel identifier. Must be alphanumeric, 6-20 characters, hyphens allowed. " +
            "Valid examples: TRK001, PKG-2024-001, NPS123456. Required — cannot be empty.",

        "Origin field: The city or facility where the parcel is being sent from. Enter a full name like 'London', " +
            "'Manchester Depot', or 'Birmingham Hub'. Required — cannot be empty.",

        "Destination field: The city or address where the parcel should be delivered. Enter the city name, " +
            "e.g. 'Edinburgh', 'Cardiff', 'Leeds'. Required — cannot be empty.",

        "Postal Code field: Determines which sorting belt the parcel is assigned to. Must start with a digit 1-6. " +
            "1x = North → Belt-A, 2x = South → Belt-B, 3x = East → Belt-C, " +
            "4x = West → Belt-D, 5x = Fragile/Oversized → Belt-E, 6x = Express → Belt-F. " +
            "Examples: 12345, 23456, 34567. Cannot be empty.",

        "Weight field: The parcel weight in kilograms. Must be a positive number greater than 0. " +
            "Parcels over 20 kg are routed to Belt-E as fragile/oversized. Maximum 70 kg. " +
            "Example: enter 2.5 for a 2.5 kg parcel. Do not enter 0 or negative values.",

        "Required field validation error: The field is empty but it is mandatory. " +
            "All five fields must be filled: Tracking Number, Origin, Destination, Postal Code, and Weight.",

        "Weight validation error: Weight must be greater than 0.1 kg. " +
            "Enter the parcel weight as a decimal number in kilograms. Example: 0.5 = 500 g, 10 = 10 kg.",

        "Postal Code validation error: Postal code must start with a digit between 1 and 6. " +
            "Letters or codes starting with 0, 7, 8, or 9 are not supported. Use a numeric code like 12345 or 34567.",

        "Tracking Number validation error: The tracking number may be too short, too long, or contain invalid characters. " +
            "Use only letters, numbers, and hyphens. Length must be 6-20 characters."
    );

    @Inject
    EmbeddingModel embeddingModel;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUser;

    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;

    private EmbeddingStore<TextSegment> store;

    void onStart(@Observes StartupEvent ev) {
        store = PgVectorEmbeddingStore.builder()
            .host(extractHost(jdbcUrl))
            .port(5432)
            .database(extractDatabase(jdbcUrl))
            .user(dbUser)
            .password(dbPassword)
            .table("field_knowledge_embeddings")
            .dimension(1536)
            .createTable(true)
            .build();

        store.removeAll();
        KNOWLEDGE.forEach(text -> {
            TextSegment segment = TextSegment.from(text);
            store.add(embeddingModel.embed(segment).content(), segment);
        });
        LOG.infof("Seeded %d knowledge entries into field_knowledge_embeddings", KNOWLEDGE.size());
    }

    public String findRelevantGuidance(String query, int maxResults) {
        try {
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(maxResults)
                .minScore(0.4)
                .build();
            EmbeddingSearchResult<TextSegment> result = store.search(request);
            if (result.matches().isEmpty()) return "No specific guidance found.";
            StringBuilder sb = new StringBuilder();
            result.matches().forEach(m -> sb.append(m.embedded().text()).append("\n"));
            return sb.toString().trim();
        } catch (Exception e) {
            LOG.warnf("Knowledge base search failed: %s", e.getMessage());
            return "No specific guidance found.";
        }
    }

    private String extractHost(String jdbcUrl) {
        return jdbcUrl.split("//")[1].split(":")[0];
    }

    private String extractDatabase(String jdbcUrl) {
        String path = jdbcUrl.split("//")[1].split("/")[1];
        return path.contains("?") ? path.split("\\?")[0] : path;
    }
}
