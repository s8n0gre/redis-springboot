package com.api.redis.repository;

import com.api.redis.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class UserRepository {
    private static final String KEY_PREFIX = "User:";

    @Autowired
    private RedisTemplate<String, User> redisTemplate;

    public void save(User user) {
        ValueOperations<String, User> ops = redisTemplate.opsForValue();
        ops.set(KEY_PREFIX + user.getId(), user, 1, TimeUnit.HOURS);
    }

    public User findById(String id) {
        ValueOperations<String, User> ops = redisTemplate.opsForValue();
        return ops.get(KEY_PREFIX + id);
    }

    public void delete(String id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }
}
