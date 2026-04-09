package com.shortener.url_shortener.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAgentParserTest {

    private final UserAgentParser parser = new UserAgentParser();

    @Test
    void parseDeviceType_mobileUserAgent_shouldReturnMobile() {
        String ua = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36";
        assertEquals("Mobile", parser.parseDeviceType(ua));
    }

    @Test
    void parseDeviceType_desktopUserAgent_shouldReturnDesktop() {
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/120.0";
        assertEquals("Desktop", parser.parseDeviceType(ua));
    }

    @Test
    void parseDeviceType_null_shouldReturnUnknown() {
        assertEquals("Unknown", parser.parseDeviceType(null));
    }

    @Test
    void parseBrowser_chrome_shouldReturnChrome() {
        String ua = "Mozilla/5.0 AppleWebKit/537.36 Chrome/120.0 Safari/537.36";
        assertEquals("Chrome", parser.parseBrowser(ua));
    }

    @Test
    void parseBrowser_firefox_shouldReturnFirefox() {
        String ua = "Mozilla/5.0 (X11; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0";
        assertEquals("Firefox", parser.parseBrowser(ua));
    }
}