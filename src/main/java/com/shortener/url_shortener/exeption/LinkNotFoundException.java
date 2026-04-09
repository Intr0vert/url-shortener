package com.shortener.url_shortener.exeption;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String code) {
        super("Link not found: " + code);
    }
}
