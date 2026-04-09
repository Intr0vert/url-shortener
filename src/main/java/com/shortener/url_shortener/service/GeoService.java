package com.shortener.url_shortener.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoService {

    private final RestTemplate restTemplate;

    public GeoService() {
        this.restTemplate = new RestTemplate();
    }

    public GeoResult lookup(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip + "?fields=status,country,city";
            GeoApiResponse response = restTemplate.getForObject(url, GeoApiResponse.class);

            if (response != null && "success".equals(response.status())) {
                return new GeoResult(response.country(), response.city());
            }
        } catch (Exception e) {
        }
        return new GeoResult(null, null);
    }

    record GeoApiResponse(String status, String country, String city) {
    }

    public record GeoResult(String country, String city) {
    }

}
