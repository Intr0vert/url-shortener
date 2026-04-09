package com.shortener.url_shortener.controller;

import java.net.URI;

import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.shortener.url_shortener.dto.LinkStatsResponse;
import com.shortener.url_shortener.model.Link;
import com.shortener.url_shortener.model.User;
import com.shortener.url_shortener.service.LinkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "Links", description = "URL shortening and redirection")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @Operation(summary = "Create short link", description = "Accepts a URL and returns a short code")
    @PostMapping("/api/shorten")
    public ResponseEntity<LinkResponse> shorten(@Valid @RequestBody ShortenRequest request,
            Authentication authentication) {
        User user = authentication != null ? (User) authentication.getPrincipal() : null;
        Link link = linkService.createLink(request.url(), user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new LinkResponse(link.getShortCode(), link.getOriginalUrl()));
    }

    @GetMapping("/api/links/{code}/stats")
    public ResponseEntity<LinkStatsResponse> getStats(@PathVariable String code) {
        LinkStatsResponse stats = linkService.getStats(code);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Redirect by short code", description = "Redirects to the original URL and tracks the click")
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Referer", required = false) String referer,
            HttpServletRequest request) {

        Link link = linkService.getByCode(code);

        String ip = request.getRemoteAddr();
        linkService.trackClick(link, ip, userAgent, referer);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(link.getOriginalUrl()))
                .build();
    }

    record ShortenRequest(
            @NotBlank(message = "URL must not be empty") @URL(message = "Must be a valid URL") String url) {
    }

    record LinkResponse(String shortCode, String originalUrl) {
    }

}
