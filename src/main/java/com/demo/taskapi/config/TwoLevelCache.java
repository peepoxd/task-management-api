package com.demo.taskapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Two-level cache: L1 (Caffeine, in-memory) → L2 (Redis, shared)
 *
 * GET: L1 hit → return | L1 miss → check L2 → put back in L1
 * PUT: write to both L1 and L2
 * EVICT: evict from both L1 and L2
 */
public class TwoLevelCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelCache.class);

    private final String name;
    private final Cache caffeineCache;  // L1
    private final Cache redisCache;     // L2

    private final AtomicLong l1Hits = new AtomicLong();
    private final AtomicLong l2Hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public TwoLevelCache(String name, Cache caffeineCache, Cache redisCache) {
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisCache = redisCache;
    }

    public long getL1Hits() { return l1Hits.get(); }
    public long getL2Hits() { return l2Hits.get(); }
    public long getMisses() { return misses.get(); }
    public long getTotalRequests() { return l1Hits.get() + l2Hits.get() + misses.get(); }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        // L1 first
        ValueWrapper value = caffeineCache.get(key);
        if (value != null) {
            l1Hits.incrementAndGet();
            log.info("Cache L1 HIT [{}] key={} (L1:{}, L2:{}, miss:{})",
                    name, key, l1Hits.get(), l2Hits.get(), misses.get());
            return value;
        }

        // L1 miss → check L2
        value = redisCache.get(key);
        if (value != null) {
            l2Hits.incrementAndGet();
            log.info("Cache L2 HIT [{}] key={}, promoting to L1 (L1:{}, L2:{}, miss:{})",
                    name, key, l1Hits.get(), l2Hits.get(), misses.get());
            caffeineCache.put(key, value.get());
            return value;
        }

        misses.incrementAndGet();
        log.info("Cache MISS [{}] key={} (L1:{}, L2:{}, miss:{})",
                name, key, l1Hits.get(), l2Hits.get(), misses.get());
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException(
                    "Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }

        // Cache miss → load value, put in both levels
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        log.debug("Cache PUT [{}] key={}", name, key);
        caffeineCache.put(key, value);
        redisCache.put(key, value);
    }

    @Override
    public void evict(Object key) {
        log.debug("Cache EVICT [{}] key={}", name, key);
        caffeineCache.evict(key);
        redisCache.evict(key);
    }

    @Override
    public void clear() {
        log.debug("Cache CLEAR [{}]", name);
        caffeineCache.clear();
        redisCache.clear();
    }
}
