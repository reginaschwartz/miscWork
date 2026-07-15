package org.example.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.JobListing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads and writes the filtered-jobs JSON file.
 *
 * Deduplication priority:
 *   1. Job ID (numeric ID extracted from the job URL) — most reliable.
 *   2. Normalised title (lower-case) — fallback when ID is unavailable.
 */
public class JobStorage {

    private final ObjectMapper objectMapper;

    public JobStorage() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Loads all jobs from the JSON file.
     * Returns an empty list if the file does not exist yet.
     */
    public List<JobListing> loadJobs(Path filePath) {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    filePath.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, JobListing.class));
        } catch (IOException e) {
            System.err.println("Could not read " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Serialises the job list to the JSON file with pretty-printing.
     */
    public void saveJobs(List<JobListing> jobs, Path filePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), jobs);
    }

    /**
     * Returns only the jobs from {@code fetched} that are not already present
     * in {@code existing}, based on job ID (primary) or normalised title (fallback).
     */
    public List<JobListing> filterNewJobs(List<JobListing> fetched, List<JobListing> existing) {
        Set<String> existingIds = existing.stream()
                .map(JobListing::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

//        Set<String> existingTitles = existing.stream()
//                .map(JobListing::getTitle)
//                .filter(t -> t != null && !t.isBlank())
//                .map(String::toLowerCase)
//                .collect(Collectors.toSet());

        return fetched.stream()
                .filter(job -> isNew(job, existingIds))
                .collect(Collectors.toList());
    }

    private boolean isNew(JobListing job, Set<String> existingIds) {
        String id = job.getId();
        if (id != null && !id.isBlank()) {
            return !existingIds.contains(id);
        }
//        String title = job.getTitle();
//        return title != null && !title.isBlank()
//                && !existingTitles.contains(title.toLowerCase());
        return false;
    }
}
