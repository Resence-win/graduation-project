package com.qms.campuscard.controller;

import com.qms.campuscard.common.Result;
import com.qms.campuscard.util.RedisUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/redis")
public class RedisTestController {

    @Resource
    private RedisUtil redisUtil;

    @GetMapping("/test")
    public Result<String> testRedis() {
        try {
            String testKey = "test:redis:connection";
            String testValue = "Hello Redis!";
            
            redisUtil.set(testKey, testValue, 60);
            Object result = redisUtil.get(testKey);
            
            return Result.success("Redis连接成功！测试值: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Redis连接失败: " + e.getMessage());
        }
    }

    @GetMapping("/clear")
    public Result<String> clearTest() {
        try {
            redisUtil.del("test:redis:connection");
            return Result.success("测试数据已清除");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("清除失败: " + e.getMessage());
        }
    }
}
