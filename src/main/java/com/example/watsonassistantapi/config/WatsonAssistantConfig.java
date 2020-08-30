package com.example.watsonassistantapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WatsonAssistantConfig {

    @Value("${watson.api-key}")
    private String apiKey;

    @Value("${watson.version}")
    private String version;

    @Value("${watson.url}")
    private String url;

    @Value("${watson.assistant-id}")
    private String assistantId;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getAssistantId() {
        return assistantId;
    }
}
