package com.shortener.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shortener.url_shortener.exeption.LinkNotFoundException;
import com.shortener.url_shortener.model.Link;
import com.shortener.url_shortener.repository.ClickRepository;
import com.shortener.url_shortener.repository.LinkRepository;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ClickRepository clickRepository;

    @Mock
    private UserAgentParser userAgentParser;

    @Mock
    private GeoService geoService;

    @InjectMocks
    private LinkService linkService;

    @Test
    void createLink_shouldSaveLinkAndReturnIt() {
        // Arrange — подготовка
        when(linkRepository.existsByShortCode(anyString())).thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act — действие
        Link result = linkService.createLink("https://github.com", null);

        // Assert — проверка
        assertNotNull(result);
        assertEquals("https://github.com", result.getOriginalUrl());
        assertNotNull(result.getShortCode());
        assertEquals(6, result.getShortCode().length());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void getByCode_existingCode_shouldReturnLink() {
        Link link = new Link("abc123", "https://github.com");
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(link));

        Link result = linkService.getByCode("abc123");

        assertEquals("https://github.com", result.getOriginalUrl());
    }

    @Test
    void getByCode_nonExistingCode_shouldThrowException() {
        when(linkRepository.findByShortCode("nope")).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> {
            linkService.getByCode("nope");
        });
    }

    @Test
    void createLink_codeCollision_shouldRetryUntilUnique() {
        // Первые два кода уже заняты, третий — свободен
        when(linkRepository.existsByShortCode(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        Link result = linkService.createLink("https://github.com", null);

        assertNotNull(result);
        // existsByShortCode вызван 3 раза (2 коллизии + 1 успех)
        verify(linkRepository, times(3)).existsByShortCode(anyString());
    }
}
