package com.fundoo.notes.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheConfigTest {

    @Test
    void testRedisCacheConfiguration() {
        RedisCacheConfiguration configuration = new CacheConfig().redisCacheConfiguration(10);
        assertEquals(Duration.ofMinutes(10), configuration.getTtlFunction().getTimeToLive("key", "value"));
        assertFalse(configuration.getAllowCacheNullValues());
        assertEquals("fundoo:notes::", configuration.getKeyPrefixFor("notes"));
    }

    @Test
    void testCachingIsEnabled() {
        assertTrue(CacheConfig.class.isAnnotationPresent(EnableCaching.class));
    }

    @Test
    void testCacheErrors_AreSwallowed() {
        CacheErrorHandler handler = new CacheConfig().errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("notes");
        RuntimeException redisDown = new RuntimeException("Unable to connect to Redis");
        assertDoesNotThrow(() -> handler.handleCacheGetError(redisDown, cache, 1L));
        assertDoesNotThrow(() -> handler.handleCachePutError(redisDown, cache, 1L, "value"));
        assertDoesNotThrow(() -> handler.handleCacheEvictError(redisDown, cache, 1L));
        assertDoesNotThrow(() -> handler.handleCacheClearError(redisDown, cache));
    }
}
