package com.api.redis.controller;

import com.api.redis.model.User;
import com.api.redis.repository.UserRepository;
import com.api.redis.repository.UserJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.redis.core.RedisTemplate;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserJdbcRepository userJdbcRepository;

    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        userRepository.save(user); // Save full User object to Redis (as before)
        userJdbcRepository.save(user); // Save to Oracle
        return ResponseEntity.ok("User saved to Redis (as object) and Oracle");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userRepository.delete(id);
        return ResponseEntity.ok("User deleted");
    }

    // Direct String set in Redis (for Postman testing)
    @PostMapping("/set")
    public ResponseEntity<String> setString(@RequestParam String key, @RequestParam String value) {
        stringRedisTemplate.opsForValue().set(key, value);
        // Also insert into Oracle DB
        User user = new User(key, value);
        userJdbcRepository.save(user);
        // Store as User object in Redis (same as /users POST)
        userRepository.save(user);
        return ResponseEntity.ok("Value set in Redis (as string and object) and Oracle");
    }

    // Direct String get from Redis (for Postman testing)
    @GetMapping("/get")
    public ResponseEntity<String> getString(@RequestParam String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }
}
