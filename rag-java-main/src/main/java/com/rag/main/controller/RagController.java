package com.rag.main.controller;

import com.rag.main.dto.IndexResponse;
import com.rag.main.dto.QueryRequest;
import com.rag.main.dto.QueryResponse;
import com.rag.main.service.IndexService;
import com.rag.main.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@Tag(name = "RAG API")
public class RagController {

    private final IndexService indexService;
    private final QueryService queryService;

    public RagController(IndexService indexService, QueryService queryService) {
        this.indexService = indexService;
        this.queryService = queryService;
    }

    @PostMapping(value = "/index", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Index a text or PDF file into PGVector")
    public IndexResponse indexDocuments(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "metadata_json", required = false) String metadataJson,
            @RequestParam(value = "reset_collection", defaultValue = "false") boolean resetCollection,
            @RequestParam(value = "context_tag", required = false) String contextTag) {
        return indexService.indexDocument(file, metadataJson, resetCollection, contextTag);
    }

    @PostMapping("/query")
    @Operation(summary = "Query indexed content with RAG")
    public QueryResponse query(@Valid @RequestBody QueryRequest request) {
        return queryService.query(request);
    }
}
