package com.rag.main.runner;

import com.rag.main.config.RagProperties;
import com.rag.main.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BulkIndexRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BulkIndexRunner.class);
    private static final String DATA_PATH = "data/books";

    private final RagProperties properties;
    private final IndexService indexService;

    public BulkIndexRunner(RagProperties properties, IndexService indexService) {
        this.properties = properties;
        this.indexService = indexService;
    }

    @Override
    public void run(String... args) {
//        if (!properties.isBulkIndex()) {
//            return;
//        }

        int documents = indexService.bulkIndexMarkdownDirectory(DATA_PATH, true);
        log.info("Bulk indexed {} markdown documents from {}.", documents, DATA_PATH);
    }
}
