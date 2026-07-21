package com.rag.main.service;

import com.rag.main.dto.QueryRequest;
import com.rag.main.dto.QueryResponse;
import com.rag.main.exception.NotFoundException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import java.util.List;
import org.springframework.stereotype.Service;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
public class QueryService {

    private static final String PROMPT_TEMPLATE =
            """
            Answer the question based only on the following context:

            %s

            ---

            Answer the question based on the above context: %s
            """;

    private final VectorStoreService vectorStoreService;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;

    public QueryService(
            VectorStoreService vectorStoreService,
            EmbeddingModel embeddingModel,
            ChatModel chatModel) {
        this.vectorStoreService = vectorStoreService;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    public QueryResponse query(QueryRequest request) {
        Embedding queryEmbedding = embeddingModel.embed(request.getQueryText()).content();

        EmbeddingSearchRequest.EmbeddingSearchRequestBuilder searchBuilder = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(request.getK())
                .minScore(request.getMinRelevance());

        if (request.getContextTag() != null && !request.getContextTag().isBlank()) {
            Filter filter = metadataKey("context_tag").isEqualTo(request.getContextTag());
            searchBuilder.filter(filter);
        }

        EmbeddingSearchResult<TextSegment> searchResult =
                vectorStoreService.getEmbeddingStore().search(searchBuilder.build());
        List<EmbeddingMatch<TextSegment>> results = searchResult.matches();

        if (results.isEmpty()) {
            throw new NotFoundException("Unable to find matching results.");
        }

        String contextText = results.stream()
                .map(match -> match.embedded().text())
                .reduce((left, right) -> left + "\n\n---\n\n" + right)
                .orElse("");

        String prompt = PROMPT_TEMPLATE.formatted(contextText, request.getQueryText());
        String responseText = chatModel.chat(prompt);

        List<String> sources = results.stream()
                .map(match -> match.embedded().metadata().getString("source"))
                .map(source -> source == null ? "" : source)
                .toList();

        return new QueryResponse(responseText, sources);
    }
}
