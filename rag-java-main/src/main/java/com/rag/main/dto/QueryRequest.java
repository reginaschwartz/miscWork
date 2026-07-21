package com.rag.main.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class QueryRequest {

    @NotBlank
    @JsonProperty("query_text")
    private String queryText;

    private int k = 3;

    @JsonProperty("min_relevance")
    private double minRelevance = 0.7;

    @JsonProperty("context_tag")
    private String contextTag;

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getMinRelevance() {
        return minRelevance;
    }

    public void setMinRelevance(double minRelevance) {
        this.minRelevance = minRelevance;
    }

    public String getContextTag() {
        return contextTag;
    }

    public void setContextTag(String contextTag) {
        this.contextTag = contextTag;
    }
}
