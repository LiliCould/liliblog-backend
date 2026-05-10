package cn.lilicould.liliblog.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类,对RedisTemplate做进一步封装
 */
@Component
@RequiredArgsConstructor
public class RedisHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    // 全局 key 前缀
    @Value("${spring.application.name}:liliblog")
    private String appName;

    private final String APP_PREFIX = appName + ":";

    private String buildKey(String key) {
        return this.APP_PREFIX + key;
    }

    // ==================== 基本 String 操作 ====================

    /**
     * 设置键值对（无过期时间，永久有效）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(buildKey(key), value);
    }

    /**
     * 设置键值对并指定过期时间
     */
    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(buildKey(key), value, timeout);
    }

    /**
     * 设置键值对并指定过期时间（毫秒）
     */
    public void set(String key, Object value, long milliseconds) {
        redisTemplate.opsForValue().set(buildKey(key), value, milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    /**
     * 获取并转换为指定类型（需确保类型匹配）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) get(key);
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(buildKey(key));
    }

    /**
     * 批量删除
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys.stream().map(this::buildKey).toList());
    }

    /**
     * 判断键是否存在
     */
    public Boolean exists(String key) {
        return redisTemplate.hasKey(buildKey(key));
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, Duration timeout) {
        return redisTemplate.expire(buildKey(key), timeout);
    }

    /**
     * 获取剩余过期时间（毫秒，-1 表示永久，-2 表示不存在）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(buildKey(key), TimeUnit.MILLISECONDS);
    }

    /**
     * 如果不存在则设置（用于分布式锁或去重）
     * @return true 表示设置成功，false 表示已存在
     */
    public Boolean setIfAbsent(String key, Object value, Duration timeout) {
        return redisTemplate.opsForValue().setIfAbsent(buildKey(key), value, timeout);
    }

    // ==================== 哈希操作 ====================

    /**
     * 设置 Hash 字段值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(buildKey(key), field, value);
    }

    /**
     * 获取 Hash 字段值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(buildKey(key), field);
    }

    /**
     * 获取整个 Hash
     */
    @SuppressWarnings("unchecked")
    public <HK, HV> java.util.Map<HK, HV> hGetAll(String key) {
        return (Map<HK, HV>) redisTemplate.opsForHash().entries(buildKey(key));
    }

    /**
     * 删除 Hash 字段
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(buildKey(key), fields);
    }

    // ==================== List 操作 ====================

    /**
     * 从左侧推入列表
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(buildKey(key), value);
    }

    /**
     * 从右侧推入列表
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(buildKey(key), value);
    }

    /**
     * 从左侧弹出列表
     */
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(buildKey(key));
    }

    /**
     * 获取列表范围内元素（start/end 支持负数）
     */
    public java.util.List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(buildKey(key), start, end);
    }

    // ==================== Set 操作 ====================

    /**
     * 添加元素到 Set
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(buildKey(key), values);
    }

    /**
     * 获取 Set 所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(buildKey(key));
    }

    /**
     * 判断成员是否在 Set 中
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(buildKey(key), value);
    }

}
