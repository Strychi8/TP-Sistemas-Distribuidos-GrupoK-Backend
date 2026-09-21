package com.empresa_rentar.web_services;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {
    @Test
    public void generateHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("HASH_123456: " + encoder.encode("123456"));
        System.out.println("HASH_ADMIN: " + encoder.encode("admin123"));
    }
}
