package com.shortener.url_shortener.service;

import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public String parseDeviceType(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android"))
            return "Mobile";
        if (ua.contains("tablet") || ua.contains("ipad"))
            return "Tablet";
        return "Desktop";
    }

    public String parseBrowser(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        if (userAgent.contains("Firefox"))
            return "Firefox";
        if (userAgent.contains("Edg"))
            return "Edge";
        if (userAgent.contains("Chrome"))
            return "Chrome";
        if (userAgent.contains("Safari"))
            return "Safari";
        return "Other";
    }

}
