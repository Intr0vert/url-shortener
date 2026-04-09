package com.shortener.url_shortener.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clicks")
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String referer;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(length = 50)
    private String browser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Click() {
    }

    public Click(Link link, String ip, String userAgent, String referer,
            String deviceType, String browser) {
        this.link = link;
        this.ip = ip;
        this.userAgent = userAgent;
        this.referer = referer;
        this.deviceType = deviceType;
        this.browser = browser;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Link getLink() {
        return link;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
