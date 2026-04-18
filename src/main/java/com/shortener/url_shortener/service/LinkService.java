package com.shortener.url_shortener.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shortener.url_shortener.dto.LinkStatsResponse;
import com.shortener.url_shortener.dto.LinkStatsResponse.DailyCount;
import com.shortener.url_shortener.dto.LinkStatsResponse.TopItem;
import com.shortener.url_shortener.exeption.LinkNotFoundException;
import com.shortener.url_shortener.model.Click;
import com.shortener.url_shortener.model.Link;
import com.shortener.url_shortener.model.User;
import com.shortener.url_shortener.repository.ClickRepository;
import com.shortener.url_shortener.repository.LinkRepository;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final ClickRepository clickRepository;
    private final UserAgentParser userAgentParser;
    private final GeoService geoService;
    private final RedisCacheService redisCacheService;

    public LinkStatsResponse getStats(String code) {
        Link link = getLinkByCode(code);
        Long linkId = link.getId();

        long totalClicks = clickRepository.countByLinkId(linkId);

        List<DailyCount> clicksByDay = clickRepository.countByDay(linkId).stream()
                .map(row -> new DailyCount(row[0].toString(), (long) row[1]))
                .toList();

        List<TopItem> topCountries = mapToTopItems(clickRepository.topCountries(linkId));
        List<TopItem> topDevices = mapToTopItems(clickRepository.topDevices(linkId));
        List<TopItem> topBrowsers = mapToTopItems(clickRepository.topBrowsers(linkId));
        List<TopItem> topReferers = mapToTopItems(clickRepository.topReferers(linkId));

        return new LinkStatsResponse(
                link.getShortCode(),
                link.getOriginalUrl(),
                totalClicks,
                clicksByDay,
                topCountries,
                topDevices,
                topBrowsers,
                topReferers);
    }

    private List<TopItem> mapToTopItems(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new TopItem((String) row[0], (long) row[1]))
                .toList();
    }

    public LinkService(LinkRepository linkRepository, ClickRepository clickRepository,
            UserAgentParser userAgentParser,
            GeoService geoService,
            RedisCacheService redisCacheService) {
        this.clickRepository = clickRepository;
        this.geoService = geoService;
        this.linkRepository = linkRepository;
        this.userAgentParser = userAgentParser;
        this.redisCacheService = redisCacheService;
    }

    public Link createLink(String originalUrl, User user) {
        String code = generateUniqueCode();
        Link link = new Link(code, originalUrl, user);
        return linkRepository.save(link);
    }

    public Link getLinkByCode(String code) {
        return linkRepository.findByShortCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));
    }

    public String getOriginalUrl(String code) {
        String cached = redisCacheService.getOriginalUrl(code);
        if (cached != null) {

            return cached;
        }

        Link link = linkRepository.findByShortCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));

        redisCacheService.cacheOriginalUrl(code, link.getOriginalUrl());

        return link.getOriginalUrl();
    }

    @Transactional
    public void deleteLink(String code, User user) {
        Link link = linkRepository.findByShortCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));

        if (link.getUser() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anonymous links cannot be deleted");
        }

        if (!link.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your link");
        }

        clickRepository.deleteByLinkId(link.getId());
        linkRepository.delete(link);
        redisCacheService.evict(code);
    }

    @Transactional
    public void trackClick(String shortCode, String ip, String userAgent, String referer) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException(shortCode));
        String deviceType = userAgentParser.parseDeviceType(userAgent);
        String browser = userAgentParser.parseBrowser(userAgent);

        Click click = new Click(link, ip, userAgent, referer, deviceType, browser);

        GeoService.GeoResult geo = geoService.lookup(ip);
        click.setCountry(geo.country());
        click.setCity(geo.city());

        clickRepository.save(click);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode(6);
        } while (linkRepository.existsByShortCode(code));
        return code;
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int charsLength = chars.length();
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(charsLength)));
        }
        return sb.toString();
    }

}
