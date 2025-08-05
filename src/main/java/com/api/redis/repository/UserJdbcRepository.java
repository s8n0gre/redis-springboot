package com.api.redis.repository;

import com.api.redis.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserJdbcRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int save(User user) {
        String insertSql = "INSERT INTO users (id, name) VALUES (?, ?)";
        String updateSql = "UPDATE users SET name = ? WHERE id = ?";
        try {
            int result = jdbcTemplate.update(insertSql, user.getId(), user.getName());
            System.out.println("User insert result: " + result);
            return result;
        } catch (Exception e) {
            // Check for unique constraint violation (ORA-00001)
            if (e.getMessage() != null && e.getMessage().contains("ORA-00001")) {
                int result = jdbcTemplate.update(updateSql, user.getName(), user.getId());
                System.out.println("User update result: " + result);
                return result;
            } else {
                System.err.println("User insert failed: " + e.getMessage());
                throw e;
            }
        }
    }
}
