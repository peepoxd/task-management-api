package com.demo.taskapi.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * CacheManager that combines L1 (Caffeine) and L2 (Redis) into TwoLevelCache.
 */
public class TwoLevelCacheManager implements CacheManager {

    private final CacheManager caffeineCacheManager;  // L1
    private final CacheManager redisCacheManager;     // L2
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

    public TwoLevelCacheManager(CacheManager caffeineCacheManager, CacheManager redisCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, cacheName -> {
            Cache l1 = caffeineCacheManager.getCache(cacheName);
            Cache l2 = redisCacheManager.getCache(cacheName);
            if (l1 == null || l2 == null) {
                return null;
            }
            return new TwoLevelCache(cacheName, l1, l2);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableCollection(cacheMap.keySet());
    }
}
