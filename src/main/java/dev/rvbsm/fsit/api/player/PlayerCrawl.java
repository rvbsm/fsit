package dev.rvbsm.fsit.api.player;

import org.jetbrains.annotations.NotNull;

import dev.rvbsm.fsit.entity.CrawlEntity;

public interface PlayerCrawl {

    void fsit$startCrawling(@NotNull CrawlEntity crawlEntity);

    void fsit$stopCrawling();

    boolean fsit$isCrawling();
}
