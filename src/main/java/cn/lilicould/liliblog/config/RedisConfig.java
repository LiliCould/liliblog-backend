package cn.lilicould.liliblog.config;


import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 创建专用于 Redis 的 ObjectMapper，不注册为 Spring Bean
     */
    private ObjectMapper createRedisObjectMapper() {
        // 创建多态类型验证器（允许所有子类型，或根据业务限制白名单）
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        return JsonMapper.builder()
                // 开启默认类型信息：对所有非 final 类型添加 "@class" 属性
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL)
                // JavaTimeModule 等已内置，无需手动注册
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 Redis 连接工厂，用于创建与 Redis 服务器的连接
        template.setConnectionFactory(connectionFactory);

        // 使用 StringRedisSerializer 作为 key 的序列化器，将 key 以字符串形式存储（可读性好）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        // 设置 hash 结构中 field 的序列化器
        template.setHashKeySerializer(stringSerializer);

        // 使用专用 ObjectMapper 创建序列化器
        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(createRedisObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 调用 afterPropertiesSet() 方法，执行初始化逻辑（检查必要属性是否设置，并完成内部组件的初始化）
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 使用专用 ObjectMapper 创建序列化器
        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(createRedisObjectMapper());

        // 配置 Redis 缓存的默认行为
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存的默认过期时间为 1 小时
                .entryTtl(Duration.ofHours(1))
                // 配置缓存键的序列化器：使用 StringRedisSerializer，将键以可读的字符串形式存储
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 配置缓存值的序列化器：使用前面创建的 JSON 序列化器
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                // 禁止缓存 null 值；如果方法返回 null，不会向 Redis 中存储占位符,但是会缓存击穿
                .disableCachingNullValues();

        // 3. 创建并返回 RedisCacheManager 实例
        return RedisCacheManager.builder(connectionFactory)
                // 应用上面定义的默认配置
                .cacheDefaults(config)
                /* 启用事务感知：如果当前有 Redis 事务，缓存操作将参与到事务中，
                保证缓存操作与数据库操作的一致性（需配合 @Transactional 使用） */
                .transactionAware()
                .build();
    }
}