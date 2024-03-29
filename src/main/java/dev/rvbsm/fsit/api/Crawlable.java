package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.entity.CrawlEntity;
import org.jetbrains.annotations.NotNull;

public interface Crawlable {
    void fsit$startCrawling(@NotNull CrawlEntity crawlEntity);

    void fsit$stopCrawling();

    boolean fsit$isCrawling();
}
