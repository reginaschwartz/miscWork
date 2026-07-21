package com.rag.main.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rag.main.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ContentExtractorTest {

    private final ContentExtractor contentExtractor = new ContentExtractor();

    @Test
    void extractsUtf8Text() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "Hello RAG".getBytes());

        String content = contentExtractor.extractContent(file, "notes.txt");

        assertEquals("Hello RAG", content);
    }

    @Test
    void rejectsEmptyTextFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(BadRequestException.class, () -> contentExtractor.extractContent(file, "empty.txt"));
    }
}
