package com.rag.main.service;

import dev.langchain4j.data.document.Metadata;
import java.util.Map;

final class MetadataMapper {

    private MetadataMapper() {
    }

    static Metadata fromMap(Map<String, String> values) {
        Metadata metadata = new Metadata();
        values.forEach(metadata::put);
        return metadata;
    }
}
