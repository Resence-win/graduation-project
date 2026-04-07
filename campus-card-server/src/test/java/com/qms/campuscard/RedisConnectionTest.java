package com.qms.campuscard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        try {
            System.out.println("========================================");
            System.out.println("正在测试Redis连接...");
            
            String testKey = "test:redis:connection";
            String testValue = "Hello Redis!";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            Object result = redisTemplate.opsForValue().get(testKey);
            
            System.out.println("✓ Redis连接成功！");
            System.out.println("✓ 测试值: " + result);
            
            redisTemplate.delete(testKey);
            System.out.println("✓ 测试数据已清除");
            System.out.println("========================================");
        } catch (Exception e) {
            System.out.println("========================================");
            System.out.println("✗ Redis连接失败：" + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            throw e;
        }
    }
}