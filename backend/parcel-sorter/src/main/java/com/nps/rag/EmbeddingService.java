package com.nps.rag;

import com.nps.entity.SortingRule;
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
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class EmbeddingService {

    private static final Logger LOG = Logger.getLogger(EmbeddingService.class);

    @Inject
    EmbeddingModel embeddingModel;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUser;

    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;

    private EmbeddingStore<TextSegment> embeddingStore;

    void onStart(@Observes StartupEvent ev) {
        embeddingStore = PgVectorEmbeddingStore.builder()
            .host(extractHost(jdbcUrl))
            .port(5432)
            .database(extractDatabase(jdbcUrl))
            .user(dbUser)
            .password(dbPassword)
            .table("sorting_rule_embeddings")
            .dimension(1536)
            .createTable(true)
            .build();
        LOG.info("PgVectorEmbeddingStore initialized");
    }

    @Transactional
    public void reindexRules() {
        List<SortingRule> rules = SortingRule.listAll();
        embeddingStore.removeAll();
        rules.stream().filter(r -> r.active).forEach(rule -> {
            String text = String.format("Belt %s handles postal code pattern '%s'. Priority: %d. %s",
                rule.assignedBelt, rule.postalCodePattern, rule.priority, rule.description);
            TextSegment segment = TextSegment.from(text);
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        });
        LOG.infof("Indexed %d sorting rules into pgvector", rules.size());
    }

    public String findRelevantRules(String query, int maxResults) {
        try {
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(maxResults)
                .minScore(0.5)
                .build();
            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

            if (result.matches().isEmpty()) return null;

            StringBuilder sb = new StringBuilder("Relevant Sorting Rules:\n");
            result.matches().forEach(m -> sb.append("- ").append(m.embedded().text())
                .append(String.format(" (score: %.2f)\n", m.score())));
            return sb.toString();
        } catch (Exception e) {
            LOG.warnf("Vector search failed: %s", e.getMessage());
            return null;
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
