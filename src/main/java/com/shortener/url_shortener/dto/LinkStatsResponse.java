package com.shortener.url_shortener.dto;

import java.util.List;

public record LinkStatsResponse(
        String shortCode,
        String originalUrl,
        long totalClicks,
        List<DailyCount> clicksByDay,
        List<TopItem> topCountries,
        List<TopItem> topDevices,
        List<TopItem> topBrowsers,
        List<TopItem> topReferers) {
    public record DailyCount(String date, long count) {
    }

    public record TopItem(String name, long count) {
    }
}
