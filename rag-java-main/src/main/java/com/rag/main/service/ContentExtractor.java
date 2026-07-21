package com.rag.main.service;

import com.rag.main.exception.BadRequestException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ContentExtractor {

    public String extractContent(MultipartFile file, String source) {
        try {
            byte[] rawBytes = file.getBytes();
            if (source.toLowerCase().endsWith(".pdf")) {
                DocumentParser parser = new ApachePdfBoxDocumentParser();
                Document document = parser.parse(new ByteArrayInputStream(rawBytes));
                String content = document.text().trim();
                if (content.isEmpty()) {
                    throw new BadRequestException("Uploaded PDF has no extractable text.");
                }
                return content;
            }

            String content = new String(rawBytes, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                throw new BadRequestException("Uploaded file is empty.");
            }
            return content;
        } catch (BadRequestException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read uploaded file.");
        } catch (RuntimeException exception) {
            throw new BadRequestException("Uploaded file must be UTF-8 text or PDF.");
        }
    }
}
