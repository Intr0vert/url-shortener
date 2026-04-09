package com.shortener.url_shortener.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shortener.url_shortener.model.User;
import com.shortener.url_shortener.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        String hashed = passwordEncoder.encode(password);
        User user = new User(email, hashed, "USER");
        userRepository.save(user);
        return jwtService.generateToken(email);
    }

    public String login(String email, String password) {
        String invalidCredentialsString = "Invalid credentials";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(invalidCredentialsString));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException(invalidCredentialsString);
        }
        return jwtService.generateToken(email);
    }
}