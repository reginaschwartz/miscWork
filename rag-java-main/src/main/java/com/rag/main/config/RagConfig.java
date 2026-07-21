package com.rag.main.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig {

    @Bean
    DataSource dataSource(RagProperties properties) {
        RagProperties.Postgres postgres = properties.getPostgres();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getPort(),
                postgres.getDatabase()));
        config.setUsername(postgres.getUser());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    EmbeddingModel embeddingModel(RagProperties properties) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(properties.getOpenai().getApiKey())
                .modelName(properties.getOpenai().getEmbeddingModel())
                .build();
    }

    @Bean
    ChatModel chatModel(RagProperties properties) {
        return OpenAiChatModel.builder()
                .apiKey(properties.getOpenai().getApiKey())
                .modelName(properties.getOpenai().getChatModel())
                .build();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource, EmbeddingModel embeddingModel) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("rag_embeddings")
                .dimension(embeddingModel.dimension())
                .createTable(true)
              //  .metadataStorageConfig(MetadataStorageConfig.combinedJsonb())
                .build();
    }
}
