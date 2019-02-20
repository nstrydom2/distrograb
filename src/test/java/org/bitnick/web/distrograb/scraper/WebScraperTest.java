package org.bitnick.web.distrograb.scraper;

import org.junit.jupiter.api.Test;

public class WebScraperTest {
    private final WebScraper webScraper = WebScraper.instanceOf();

    @Test
    public void collectsAllDistroDownloadData() throws Exception {
        webScraper.getDistroList();
    }
}
