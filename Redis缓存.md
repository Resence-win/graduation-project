# Redis缓存设计文档

## 一、Redis部署与配置

### 1.1 Redis部署

Redis已部署在本地，默认配置如下：
- 主机：localhost
- 端口：6379
- 数据库：0
- 无密码认证

### 1.2 项目配置

在 `application.yml` 中配置Redis：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-wait: -1
          max-idle: 8
          min-idle: 0
```

### 1.3 Redis配置类

文件位置：`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/config/RedisConfig.java`

**作用**：配置RedisTemplate的序列化方式，使用Jackson2JsonRedisSerializer序列化Java对象，StringRedisSerializer序列化键。

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用Jackson2JsonRedisSerializer序列化值
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                                         ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        
        // 使用StringRedisSerializer序列化键
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

### 1.4 Redis工具类

文件位置：`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/util/RedisUtil.java`

**作用**：封装Redis常用操作方法，提供对String、Hash、List、Set等数据结构的操作支持。

主要方法：
- `set(key, value, timeout)`：设置键值对，带过期时间
- `get(key)`：获取键对应的值
- `del(key)`：删除键
- `hset(key, field, value)`：设置Hash值
- `hget(key, field)`：获取Hash值
- `lset(key, index, value)`：设置List值
- `sadd(key, values)`：添加Set值
- 等等...

---

## 二、缓存键命名规范

### 2.1 命名规则

格式：`{模块}:{类型}:{标识}`

- **模块**：card、account、merchant、commute等
- **类型**：info、balance、list、route等
- **标识**：具体的ID、卡号等

### 2.2 缓存键列表

| 缓存键 | 数据类型 | 说明 | 过期时间 |
|--------|----------|------|----------|
| `card:info:{cardId}` | Object | 校园卡信息（按ID） | 1800秒 |
| `card:info:no:{cardNo}` | Object | 校园卡信息（按卡号） | 1800秒 |
| `account:balance:{cardId}` | BigDecimal | 账户余额 | 1800秒 |
| `merchant:info:{merchantId}` | Object | 商户信息 | 3600秒 |
| `merchant:type:list` | List | 商户类型列表 | 3600秒 |
| `commute:route:{routeId}` | Object | 线路信息 | 3600秒 |
| `commute:route:list` | List | 线路列表 | 3600秒 |
| `commute:vehicle:{vehicleId}` | Object | 车辆信息 | 3600秒 |
| `commute:vehicle:list` | List | 车辆列表 | 3600秒 |
| `commute:schedule:{scheduleId}` | Object | 班次信息 | 3600秒 |
| `commute:schedule:list` | List | 班次列表 | 3600秒 |
| `commute:schedule:route:{routeId}` | List | 线路班次列表 | 3600秒 |

---

## 三、各模块缓存实现

### 3.1 校园卡模块

#### 3.1.1 实现位置

`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/CampusCardServiceImpl.java`

#### 3.1.2 缓存内容

1. **校园卡信息缓存**
   - 缓存键：`card:info:{cardId}`、`card:info:no:{cardNo}`
   - 数据：CampusCardDTO对象
   - 过期时间：1800秒

2. **账户余额缓存**
   - 缓存键：`account:balance:{cardId}`
   - 数据：BigDecimal类型余额
   - 过期时间：1800秒

#### 3.1.3 缓存更新

- **查询时**：先从Redis获取，未命中则查数据库并写入缓存
- **更新时**：挂失、解挂、注销操作后清除相关缓存
- **消费/充值后**：清除余额缓存

#### 3.1.4 清除缓存方法

```java
private void clearCardCache(Long cardId, String cardNo) {
    redisUtil.del(CARD_INFO_KEY_PREFIX + cardId);
    redisUtil.del(CARD_INFO_NO_KEY_PREFIX + cardNo);
    redisUtil.del(ACCOUNT_BALANCE_KEY_PREFIX + cardId);
}
```

---

### 3.2 商户模块

#### 3.2.1 实现位置

`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/MerchantServiceImpl.java`

#### 3.2.2 缓存内容

1. **商户信息缓存**
   - 缓存键：`merchant:info:{merchantId}`
   - 数据：Merchant对象
   - 过期时间：3600秒

2. **商户类型列表缓存**
   - 缓存键：`merchant:type:list`
   - 数据：List<MerchantType>
   - 过期时间：3600秒

#### 3.2.3 缓存更新

- **查询时**：先从Redis获取，未命中则查数据库并写入缓存
- **更新时**：添加、修改、删除商户类型或商户后清除相关缓存

---

### 3.3 通勤车模块

#### 3.3.1 线路缓存

**实现位置**：`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/CommuteRouteServiceImpl.java`

**缓存内容**：
- 线路信息：`commute:route:{routeId}`
- 线路列表：`commute:route:list`
- 过期时间：3600秒

**新增接口**：
```java
List<CommuteRoute> getAllRoutes();
```

---

#### 3.3.2 车辆缓存

**实现位置**：`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/CommuteVehicleServiceImpl.java`

**缓存内容**：
- 车辆信息：`commute:vehicle:{vehicleId}`
- 车辆列表：`commute:vehicle:list`
- 过期时间：3600秒

**新增接口**：
```java
List<CommuteVehicle> getAllVehicles();
```

---

#### 3.3.3 班次缓存

**实现位置**：`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/CommuteScheduleServiceImpl.java`

**缓存内容**：
- 班次信息：`commute:schedule:{scheduleId}`
- 班次列表：`commute:schedule:list`
- 线路班次：`commute:schedule:route:{routeId}`
- 过期时间：3600秒

**新增接口**：
```java
List<CommuteSchedule> getAllSchedules();
List<CommuteSchedule> getSchedulesByRouteId(Long routeId);
```

---

### 3.4 消费模块

#### 3.4.1 实现位置

`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/ConsumeServiceImpl.java`

#### 3.4.2 缓存更新

消费成功后，清除账户余额缓存：
```java
redisUtil.del(ACCOUNT_BALANCE_KEY_PREFIX + cardId);
```

---

### 3.5 充值模块

#### 3.5.1 实现位置

`/Users/qms/graduation/campus-card-server/src/main/java/com/qms/campuscard/service/impl/RechargeServiceImpl.java`

#### 3.5.2 缓存更新

充值成功后，清除账户余额缓存：
```java
redisUtil.del(ACCOUNT_BALANCE_KEY_PREFIX + cardId);
```

---

## 四、缓存策略总结

### 4.1 查询策略

```
用户请求
    ↓
查询Redis缓存
    ↓
命中？ → 是 → 返回缓存数据
    ↓ 否
查询数据库
    ↓
写入Redis缓存
    ↓
返回数据
```

### 4.2 更新策略

```
用户更新请求
    ↓
更新数据库
    ↓
删除相关Redis缓存
    ↓
返回成功
```

### 4.3 删除策略

```
用户删除请求
    ↓
更新数据库（软删除）
    ↓
删除相关Redis缓存
    ↓
返回成功
```

---

## 五、缓存优势

1. **提升响应速度**：热点数据直接从Redis获取，减少数据库查询
2. **降低数据库压力**：减少数据库IO操作，提升系统稳定性
3. **支持高并发**：Redis具有高性能读写能力，适合高并发场景
4. **灵活的过期机制**：通过TTL设置自动过期，保证数据新鲜度
5. **数据一致性**：更新操作后立即清除缓存，确保数据一致性

---

## 六、注意事项

1. **缓存穿透**：对于不存在的数据，缓存空值防止穿透
2. **缓存雪崩**：设置不同的过期时间，避免大量key同时过期
3. **缓存击穿**：对于热点数据，考虑使用互斥锁防止击穿
4. **数据一致性**：更新操作务必清除相关缓存
5. **内存管理**：定期监控Redis内存使用，及时清理过期数据

---

## 七、后续优化方向

1. **学生/教师信息缓存**：将学生和教师信息加入缓存
2. **缓存预热**：系统启动时预加载热点数据
3. **分布式锁**：使用Redis实现分布式锁，保证并发安全
4. **缓存监控**：添加缓存命中率监控，优化缓存策略
5. **多级缓存**：结合本地缓存（Caffeine）和Redis，进一步提升性能

