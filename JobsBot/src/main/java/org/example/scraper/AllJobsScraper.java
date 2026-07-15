package org.example.scraper;

import org.example.model.JobListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllJobsScraper {

    private static final String BASE_URL = "https://www.alljobs.co.il";
    private static final String SEARCH_URL_TEMPLATE =
            BASE_URL + "/SearchResultsGuest.aspx?page=%d&position=1994,1902,1929,1759" +
            "&type=&source=&duration=0&exc=&region=" +
            "&utm_source=alljobs&utm_medium=unbounce&utm_content=backend&utm_campaign=came_from_high_tech_roles_lp";

    private static final Pattern JOB_ID_FROM_URL = Pattern.compile("[Jj]ob[Ii][Dd]=(\\d+)");

    /** Reusable SSL socket factory that accepts any certificate (avoids PKIX errors). */
    private static final SSLSocketFactory TRUST_ALL_SSL = buildTrustAllSslFactory();

    /**
     * Scrapes the alljobs.co.il search results across multiple pages.
     * A 1.5-second pause is inserted between page requests to avoid rate-limiting.
     */
    public List<JobListing> scrapeAllPages(int maxPages) {
        List<JobListing> allJobs = new ArrayList<>();
        for (int page = 1; page <= maxPages; page++) {
            String url = String.format(SEARCH_URL_TEMPLATE, page);
            System.out.printf("Fetching page %d: %s%n", page, url);
            List<JobListing> pageJobs = scrapePage(url);
            if (pageJobs.isEmpty()) {
                System.out.printf("No job cards found on page %d — stopping.%n", page);
                break;
            }
            System.out.printf("  Found %d job cards on page %d.%n", pageJobs.size(), page);
            allJobs.addAll(pageJobs);
            sleepBetweenRequests();
        }
        return allJobs;
    }

    private List<JobListing> scrapePage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                               "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "he-IL,he;q=0.9,en-US;q=0.8,en;q=0.7")
                //    .referrer(BASE_URL + "/")
                    .timeout(30_000)
                    .sslSocketFactory(TRUST_ALL_SSL)
                    .get();

            return parseJobCards(doc);
        } catch (IOException e) {
            System.err.println("Failed to fetch " + url + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * alljobs.co.il renders each job card inside:
     *   <div id="job-box-container{JobID}" class="open-board">
     *     <div class="job-box ...">
     *       <div id="job-box{JobID}"> ... </div>
     *     </div>
     *   </div>
     */
    private List<JobListing> parseJobCards(Document doc) {
        Elements jobCards = doc.select("div[id^=job-box-container]");

        if (jobCards.isEmpty()) {
            System.err.println("  Warning: no job card elements found — HTML structure may have changed.");
        } else {
            System.out.printf("  Raw job-box-container divs found: %d%n", jobCards.size());
        }

        return jobCards.stream()
                .map(this::parseJobCard)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<JobListing> parseJobCard(Element container) {
        // ── Title ──────────────────────────────────────────────────────────────
        // <div class="job-content-top-title">
        //   <div><a href="/Search/UploadSingle.aspx?JobID=8721355"><h2>Title</h2></a></div>
        //   <div class="T14"><a href="...">Company</a></div>
        // </div>
        Element titleEl = container.selectFirst("div.job-content-top-title h2");
        if (titleEl == null) return Optional.empty();

        String title = titleEl.text().trim();
        if (title.isEmpty()) return Optional.empty();

        // ── URL & Job ID ───────────────────────────────────────────────────────
        Element titleLink = container.selectFirst("div.job-content-top-title a[href*=JobID]");
        String jobUrl = "";
        if (titleLink != null) {
            String href = titleLink.attr("href");
            jobUrl = href.startsWith("http") ? href : BASE_URL + href;
        }
        String jobId = extractJobIdFromUrl(jobUrl);

        // Fallback: id is "job-box-container8721355" → strip the prefix to get the digits
        if (jobId.isEmpty()) {
            jobId = container.id().replace("job-box-container", "");
        }

        // ── Company ────────────────────────────────────────────────────────────
        // Second <div> inside job-content-top-title, class="T14"
        Element companyEl = container.selectFirst("div.job-content-top-title .T14 a");
        String company = companyEl != null ? companyEl.text().trim() : "";

        // ── Location ───────────────────────────────────────────────────────────
        // <div class="job-content-top-location"><b>מיקום המשרה: </b><a>City</a></div>
        Element locationEl = container.selectFirst("div.job-content-top-location a");
        String location = locationEl != null ? locationEl.text().trim() : "";

        // ── Description & Requirements ─────────────────────────────────────────
        // <div class="job-content-top-desc AR RTL">
        //   <div>
        //     Description text...
        //     <div class="PT15"><b>דרישות:</b><br>Requirements...</div>
        //   </div>
        // </div>
        String[] descAndReq = extractDescriptionAndRequirements(container);

        return Optional.of(JobListing.builder()
                .id(jobId)
                .title(title)
                .company(company)
                .location(location)
                .description(descAndReq[0])
                .requirements(descAndReq[1])
                .url(jobUrl)
                .savedAt(LocalDateTime.now())
                .build());
    }

    /**
     * Extracts description and requirements from the job card's content div.
     *
     * The HTML separates description from requirements using a nested PT15 div:
     *   <div class="job-content-top-desc AR RTL">
     *     <div>
     *       free-text description
     *       <div class="PT15"><b>דרישות:</b><br>requirements text</div>
     *     </div>
     *   </div>
     *
     * @return two-element array: [description, requirements]
     */
    private String[] extractDescriptionAndRequirements(Element container) {
        Element descBlock = container.selectFirst("div.job-content-top-desc");
        if (descBlock == null) {
            return new String[]{"", ""};
        }

        // Requirements live inside the PT15 child div
        Element reqBlock = descBlock.selectFirst("div.PT15");
        String requirements = "";
        if (reqBlock != null) {
            // Remove the bold "דרישות:" label, keep the rest
            reqBlock.select("b").remove();
            requirements = reqBlock.text().trim();
            // Remove the requirements block so the remaining text is pure description
            reqBlock.remove();
        }

        String description = descBlock.text().trim();
        return new String[]{description, requirements};
    }

    private String extractJobIdFromUrl(String url) {
        Matcher m = JOB_ID_FROM_URL.matcher(url);
        return m.find() ? m.group(1) : "";
    }

    private void sleepBetweenRequests() {
        try {
            Thread.sleep(1_500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates an SSLSocketFactory that trusts every certificate, bypassing
     * PKIX chain validation. Safe to use when fetching public read-only pages.
     */
    private static SSLSocketFactory buildTrustAllSslFactory() {
        try {
            TrustManager[] trustAll = {new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] chain, String authType) { /* trust all */ }
                public void checkServerTrusted(X509Certificate[] chain, String authType) { /* trust all */ }
            }};
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new java.security.SecureRandom());
            return ctx.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to initialise trust-all SSL context", e);
        }
    }
}
