package com.nps.rag;

import com.nps.entity.SortingRule;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class RagService {

    private static final Logger LOG = Logger.getLogger(RagService.class);

    @Inject
    ChatModel chatModel;

    @Inject
    EmbeddingService embeddingService;

    @Transactional
    public String askWithContext(String question) {
        return chat(buildPrompt(getContext(question), question));
    }

    @Transactional
    public String decideSortingBelt(String postalCode, double weightKg, String destination) {
        String question = String.format(
            "Parcel: postal_code=%s, weight=%.2fkg, destination=%s. Which belt? Reply with belt name and brief reason.",
            postalCode, weightKg, destination);
        return chat(buildPrompt(getContext(question), question));
    }

    private String getContext(String query) {
        String vectorContext = embeddingService.findRelevantRules(query, 3);
        if (vectorContext != null) return vectorContext;

        List<SortingRule> rules = SortingRule.listAll();
        if (rules.isEmpty()) return "No sorting rules configured.";
        StringBuilder sb = new StringBuilder("Sorting Rules:\n");
        rules.forEach(r -> sb.append(String.format("- Belt %s: postal pattern '%s', priority %d. %s\n",
            r.assignedBelt, r.postalCodePattern, r.priority, r.description)));
        return sb.toString();
    }

    private String buildPrompt(String context, String question) {
        return String.format("""
            You are an expert parcel sorting assistant for a terminal sorting facility.
            Use the following sorting rules as context to answer questions accurately.

            CONTEXT:
            %s

            QUESTION: %s

            Provide a concise, actionable answer.""", context, question);
    }

    private String chat(String prompt) {
        try {
            LOG.infof("Calling LLM via quarkus-langchain4j");
            return chatModel.chat(prompt);
        } catch (Exception e) {
            LOG.errorf("LLM call failed: %s", e.getMessage());
            return "AI service unavailable: " + e.getMessage();
        }
    }
}
