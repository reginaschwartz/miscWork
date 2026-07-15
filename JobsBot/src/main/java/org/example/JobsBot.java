package org.example;

import org.example.filter.KeywordFilter;
import org.example.model.JobListing;
import org.example.scraper.AllJobsScraper;
import org.example.storage.JobStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Orchestrates scraping → filtering → deduplication → saving.
 *
 * Flow:
 *  1. Load the existing jobs file (if any) to build the deduplication set.
 *  2. Scrape the alljobs.co.il search results for the configured number of pages.
 *  3. Keep only jobs that match at least 3 of the target keywords.
 *  4. Keep only jobs not already present in the file.
 *  5. Append the new jobs to the file and print a summary.
 */
public class JobsBot {

    private final AllJobsScraper scraper;
    private final KeywordFilter  filter;
    private final JobStorage     storage;
    private final Path           outputFile;
    private final int            maxPages;

    public JobsBot(Path outputFile, int maxPages) {
        this.scraper    = new AllJobsScraper();
        this.filter     = new KeywordFilter();
        this.storage    = new JobStorage();
        this.outputFile = outputFile;
        this.maxPages   = maxPages;
    }

    public void run() {
        System.out.println("=== JobsBot started ===");
        System.out.println("Output file : " + outputFile.toAbsolutePath());
        System.out.println("Pages to fetch: " + maxPages);
        System.out.println();

        List<JobListing> existingJobs = storage.loadJobs(outputFile);
        System.out.println("Existing jobs in file: " + existingJobs.size());

        List<JobListing> scraped = scraper.scrapeAllPages(maxPages);
        System.out.println("Total jobs scraped    : " + scraped.size());

        List<JobListing> matched = scraped.stream()
                .filter(filter::matches)
                .toList();
        System.out.println("Jobs passing keyword filter: " + matched.size());

        List<JobListing> newJobs = storage.filterNewJobs(matched, existingJobs);
        System.out.println("New jobs (not yet saved)   : " + newJobs.size());

        if (newJobs.isEmpty()) {
            System.out.println("\nNo new jobs to save. Done.");
            return;
        }

        existingJobs.addAll(newJobs);
        try {
            storage.saveJobs(existingJobs, outputFile);
            System.out.println("\nSaved " + existingJobs.size() + " total job(s) to " + outputFile);
            System.out.println("\nNew jobs added this run:");
            newJobs.forEach(job ->
                System.out.printf("  [%s] %s  @  %s%n", job.getId(), job.getTitle(), job.getCompany()));
        } catch (IOException e) {
            System.err.println("Failed to save jobs: " + e.getMessage());
        }

        System.out.println("\n=== JobsBot finished ===");
    }
}
