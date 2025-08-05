package com.api.redis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class OracleConnectionVerifier {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void verifyConnection() {
        try {
            jdbcTemplate.execute("SELECT 1 FROM DUAL");
            System.out.println("Oracle connection verified: Success");
        } catch (Exception e) {
            System.err.println("Oracle connection failed: " + e.getMessage());
        }
    }
}
