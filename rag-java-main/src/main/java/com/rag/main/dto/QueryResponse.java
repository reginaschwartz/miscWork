package com.rag.main.dto;

import java.util.List;

public class QueryResponse {

    private String response;
    private List<String> sources;

    public QueryResponse() {
    }

    public QueryResponse(String response, List<String> sources) {
        this.response = response;
        this.sources = sources;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
