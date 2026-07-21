package com.rag.main.service;

import com.rag.main.config.RagProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final JdbcTemplate jdbcTemplate;
    private final RagProperties properties;

    public VectorStoreService(
            EmbeddingStore<TextSegment> embeddingStore,
            JdbcTemplate jdbcTemplate,
            RagProperties properties) {
        this.embeddingStore = embeddingStore;
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        ensureContextTagIndex();
        log.info("Vector store initialized for collection '{}'.", getCollectionName());
    }

    public EmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }

    public String getCollectionName() {
        return properties.getCollection();
    }

    public void resetCollection() {
        embeddingStore.removeAll();
    }

    private void ensureContextTagIndex() {
        jdbcTemplate.execute(
                """
                DO $$
                BEGIN
                    IF to_regclass('public.rag_embeddings') IS NOT NULL THEN
                        CREATE INDEX IF NOT EXISTS ix_rag_embeddings_context_tag
                        ON rag_embeddings ((metadata->>'context_tag'));
                    END IF;
                END $$;
                """);
    }
}
