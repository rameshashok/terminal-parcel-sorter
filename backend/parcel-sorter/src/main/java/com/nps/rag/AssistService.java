package com.nps.rag;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AssistService {

    private static final Logger LOG = Logger.getLogger(AssistService.class);

    @Inject
    ChatModel chatModel;

    @Inject
    KnowledgeBaseService knowledgeBaseService;

    public String assist(String field, String errorCode, String value, String formContext) {
        String query = String.format("help with %s field error %s value %s", field, errorCode, value);
        String knowledge = knowledgeBaseService.findRelevantGuidance(query, 3);
        String prompt = buildPrompt(knowledge, field, errorCode, value, formContext);
        try {
            LOG.infof("Requesting AI assistance for field='%s' error='%s'", field, errorCode);
            return chatModel.chat(prompt);
        } catch (Exception e) {
            LOG.errorf("AI assist failed: %s", e.getMessage());
            return "AI guidance unavailable. " + fallback(field, errorCode);
        }
    }

    private String buildPrompt(String knowledge, String field, String errorCode, String value, String formContext) {
        return String.format("""
            You are a friendly assistant helping a user fill out a parcel shipping form.

            FIELD KNOWLEDGE:
            %s

            The user has a problem:
            - Field: %s
            - Value they entered: "%s"
            - Validation error: %s
            - Other fields filled so far: %s

            Reply in 2-3 short sentences. Be friendly and specific:
            1. Say what the problem is
            2. Tell them exactly what to enter
            3. Give a quick example if helpful

            No technical jargon. No bullet points. Plain conversational text.""",
            knowledge, field, value, errorCode, formContext);
    }

    private String fallback(String field, String errorCode) {
        if ("required".equals(errorCode)) return "This field is required — please fill it in.";
        if ("min".equals(errorCode)) return "The value is too low — please enter a larger number.";
        if (field.equalsIgnoreCase("postalCode")) return "Postal code must start with a digit between 1 and 6.";
        if (field.equalsIgnoreCase("weightKg")) return "Weight must be greater than 0 kg.";
        return "Please check the value and try again.";
    }
}
