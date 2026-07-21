package com.rag.main.dto;

public class IndexResponse {

    private int documents;
    private int chunks;
    private String collection;

    public IndexResponse() {
    }

    public IndexResponse(int documents, int chunks, String collection) {
        this.documents = documents;
        this.chunks = chunks;
        this.collection = collection;
    }

    public int getDocuments() {
        return documents;
    }

    public void setDocuments(int documents) {
        this.documents = documents;
    }

    public int getChunks() {
        return chunks;
    }

    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
