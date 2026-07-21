package com.rag.main.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.main.dto.IndexResponse;
import com.rag.main.exception.BadRequestException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IndexService {

    private static final DocumentSplitter SPLITTER = DocumentSplitters.recursive(300, 100);

    private final ContentExtractor contentExtractor;
    private final VectorStoreService vectorStoreService;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    public IndexService(
            ContentExtractor contentExtractor,
            VectorStoreService vectorStoreService,
            EmbeddingModel embeddingModel,
            ObjectMapper objectMapper) {
        this.contentExtractor = contentExtractor;
        this.vectorStoreService = vectorStoreService;
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;
    }

    public IndexResponse indexDocument(
            MultipartFile file,
            String metadataJson,
            boolean resetCollection,
            String contextTag) {
        Map<String, String> metadata = parseMetadata(metadataJson);
        String resolvedSource = file.getOriginalFilename() != null ? file.getOriginalFilename() : "api_document";
        metadata.putIfAbsent("source", resolvedSource);
        applyContextTag(metadata, contextTag);

        String content = contentExtractor.extractContent(file, resolvedSource);
        Document document = Document.from(content, MetadataMapper.fromMap(metadata));
        List<TextSegment> chunks = SPLITTER.split(document);

        if (resetCollection) {
            vectorStoreService.resetCollection();
        }

        ingestDocuments(List.of(document));

        return new IndexResponse(1, chunks.size(), vectorStoreService.getCollectionName());
    }

    public int bulkIndexMarkdownDirectory(String directoryPath, boolean resetCollection) {
        Path root = Paths.get(directoryPath);
        if (!Files.isDirectory(root)) {
            throw new BadRequestException("Bulk index directory not found: " + directoryPath);
        }

        List<Document> documents = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".md")).forEach(path -> {
                try {
                    String content = Files.readString(path);
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("source", path.getFileName().toString());
                            documents.add(Document.from(content, MetadataMapper.fromMap(metadata)));
                } catch (IOException exception) {
                    throw new BadRequestException("Unable to read markdown file: " + path);
                }
            });
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read bulk index directory: " + directoryPath);
        }

        if (documents.isEmpty()) {
            throw new BadRequestException("No markdown files found in: " + directoryPath);
        }

        if (resetCollection) {
            vectorStoreService.resetCollection();
        }

        ingestDocuments(documents);
        return documents.size();
    }

    private void ingestDocuments(List<Document> documents) {
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(SPLITTER)
                .embeddingModel(embeddingModel)
                .embeddingStore(vectorStoreService.getEmbeddingStore())
                .build();
        ingestor.ingest(documents);
    }

    private Map<String, String> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> loaded = objectMapper.readValue(metadataJson, new TypeReference<>() {});
            if (loaded == null) {
                throw new BadRequestException("metadata_json must be a JSON object.");
            }
            Map<String, String> metadata = new HashMap<>();
            for (Map.Entry<String, Object> entry : loaded.entrySet()) {
                metadata.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
            return metadata;
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadRequestException("metadata_json must be a JSON object.");
        }
    }

    private void applyContextTag(Map<String, String> metadata, String contextTag) {
        if (contextTag != null && !contextTag.isBlank()) {
            metadata.put("context_tag", contextTag);
        }
    }
}
