package cn.lilicould.liliblog.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RedisHelper缓存工具测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisHelperTest {

    private static final String PREFIX = "liliblog:";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @Mock
    private ListOperations<String, Object> listOps;

    @Mock
    private SetOperations<String, Object> setOps;

    @InjectMocks
    private RedisHelper redisHelper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisHelper, "APP_PREFIX", PREFIX);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    void increment() {
        when(valueOps.increment(PREFIX + "count")).thenReturn(5L);

        assertEquals(5L, redisHelper.increment("count"));
    }

    @Test
    void expireWithTimeUnit() {
        when(redisTemplate.expire(PREFIX + "key", 10L, TimeUnit.MINUTES)).thenReturn(true);

        assertTrue(redisHelper.expire("key", 10L, TimeUnit.MINUTES));
    }

    @Test
    void setWithoutTimeout() {
        redisHelper.set("key", "value");

        verify(valueOps).set(PREFIX + "key", "value");
    }

    @Test
    void setWithDuration() {
        Duration duration = Duration.ofSeconds(30);
        redisHelper.set("key", "value", duration);

        verify(valueOps).set(PREFIX + "key", "value", duration);
    }

    @Test
    void setWithMilliseconds() {
        redisHelper.set("key", "value", 5000L);

        verify(valueOps).set(PREFIX + "key", "value", 5000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void get() {
        when(valueOps.get(PREFIX + "key")).thenReturn("value");

        assertEquals("value", redisHelper.get("key"));
    }

    @Test
    void getWithClass() {
        when(valueOps.get(PREFIX + "key")).thenReturn("value");

        assertEquals("value", redisHelper.get("key", String.class));
    }

    @Test
    void deleteSingle() {
        when(redisTemplate.delete(PREFIX + "key")).thenReturn(true);

        assertTrue(redisHelper.delete("key"));
    }

    @Test
    void deleteCollection() {
        when(redisTemplate.delete(List.of(PREFIX + "a", PREFIX + "b"))).thenReturn(2L);

        assertEquals(2L, redisHelper.delete(List.of("a", "b")));
    }

    @Test
    void exists() {
        when(redisTemplate.hasKey(PREFIX + "key")).thenReturn(true);

        assertTrue(redisHelper.exists("key"));
        assertFalse(redisHelper.exists("none"));
    }

    @Test
    void expireWithDuration() {
        Duration duration = Duration.ofMinutes(5);
        when(redisTemplate.expire(PREFIX + "key", duration)).thenReturn(true);

        assertTrue(redisHelper.expire("key", duration));
    }

    @Test
    void getExpire() {
        when(redisTemplate.getExpire(PREFIX + "key", TimeUnit.MILLISECONDS)).thenReturn(60000L);

        assertEquals(60000L, redisHelper.getExpire("key"));
    }

    @Test
    void setIfAbsent() {
        Duration duration = Duration.ofSeconds(10);
        when(valueOps.setIfAbsent(PREFIX + "lock", "1", duration)).thenReturn(true);

        assertTrue(redisHelper.setIfAbsent("lock", "1", duration));
    }

    @Test
    void hSetAndHGet() {
        redisHelper.hSet("map", "f1", "v1");
        when(hashOps.get(PREFIX + "map", "f1")).thenReturn("v1");

        assertEquals("v1", redisHelper.hGet("map", "f1"));
    }

    @Test
    void hGetAll() {
        Map<String, String> entries = Map.of("f1", "v1");
        when(hashOps.entries(PREFIX + "map")).thenReturn(Map.of("f1", "v1"));

        Map<String, String> result = redisHelper.hGetAll("map");
        assertEquals(entries.get("f1"), result.get("f1"));
    }

    @Test
    void hDelete() {
        when(hashOps.delete(PREFIX + "map", "f1", "f2")).thenReturn(2L);

        assertEquals(2L, redisHelper.hDelete("map", "f1", "f2"));
    }

    @Test
    void lPushAndPop() {
        when(listOps.leftPush(PREFIX + "list", "a")).thenReturn(1L);
        when(listOps.rightPush(PREFIX + "list", "b")).thenReturn(2L);
        when(listOps.leftPop(PREFIX + "list")).thenReturn("a");

        assertEquals(1L, redisHelper.lLeftPush("list", "a"));
        assertEquals(2L, redisHelper.lRightPush("list", "b"));
        assertEquals("a", redisHelper.lLeftPop("list"));
    }

    @Test
    void lRange() {
        when(listOps.range(PREFIX + "list", 0L, -1L)).thenReturn(List.of("a", "b"));

        List<Object> result = redisHelper.lRange("list", 0L, -1L);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
    }

    @Test
    void sOperations() {
        when(setOps.add(PREFIX + "set", "a", "b")).thenReturn(2L);
        when(setOps.members(PREFIX + "set")).thenReturn(Set.of("a", "b"));
        when(setOps.isMember(PREFIX + "set", "a")).thenReturn(true);

        assertEquals(2L, redisHelper.sAdd("set", "a", "b"));
        assertEquals(Set.of("a", "b"), redisHelper.sMembers("set"));
        assertTrue(redisHelper.sIsMember("set", "a"));
    }

    @Test
    void keyPrefixAppliedToAllKeys() {
        redisHelper.set("key", "value");

        verify(valueOps).set("liliblog:key", "value");

        when(valueOps.get("liliblog:key")).thenReturn(null);
        assertNull(redisHelper.get("key"));
    }
}