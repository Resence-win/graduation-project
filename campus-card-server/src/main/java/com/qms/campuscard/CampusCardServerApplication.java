package com.qms.campuscard;

import com.qms.campuscard.util.RedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.qms.campuscard.mapper")
public class CampusCardServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusCardServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner testRedisConnection(RedisUtil redisUtil) {
        return args -> {
            try {
                System.out.println("========================================");
                System.out.println("正在测试Redis连接...");
                
                String testKey = "system:redis:test";
                String testValue = "Redis连接测试成功";
                
                redisUtil.set(testKey, testValue, 60);
                Object result = redisUtil.get(testKey);
                
                if (testValue.equals(result)) {
                    System.out.println("✓ Redis连接成功！");
                    System.out.println("✓ 测试值: " + result);
                    redisUtil.del(testKey);
                } else {
                    System.out.println("✗ Redis连接失败：值不匹配");
                }
                
                System.out.println("========================================");
            } catch (Exception e) {
                System.out.println("========================================");
                System.out.println("✗ Redis连接失败：" + e.getMessage());
                e.printStackTrace();
                System.out.println("========================================");
            }
        };
    }
}

