package com.shortener.url_shortener.controller;

import com.shortener.url_shortener.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class LinkControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void fullFlow_createLinkAndRedirect() throws Exception {
        // 1. Регистрация
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "integration@test.com", "password": "123456"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // 2. Получаем токен
        String token = jwtService.generateToken("integration@test.com");

        // 3. Создаём ссылку
        String response = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("""
                        {"url": "https://github.com"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Достаём shortCode из ответа
        String shortCode = response.split("\"shortCode\":\"")[1].split("\"")[0];

        // 4. Редирект
        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://github.com"));

        // 5. Несуществующий код — 404
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLink_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"url": "https://github.com"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createLink_invalidUrl_shouldReturn400() throws Exception {
        String token = jwtService.generateToken("integration@test.com");

        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("""
                        {"url": "not-a-url"}
                        """))
                .andExpect(status().isBadRequest());
    }
}