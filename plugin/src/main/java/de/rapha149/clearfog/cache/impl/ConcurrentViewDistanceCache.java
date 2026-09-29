package de.rapha149.clearfog.cache.impl;

import de.rapha149.clearfog.cache.ViewDistanceCache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentViewDistanceCache implements ViewDistanceCache {

    private final Map<UUID, Integer> cache = new ConcurrentHashMap<>();

    @Override
    public int get(UUID playerId) {
        Integer val = cache.get(playerId);
        return val != null ? val : -1;
    }

    @Override
    public void put(UUID playerId, int viewDistance) {
        cache.put(playerId, viewDistance);
    }

    @Override
    public void remove(UUID playerId) {
        cache.remove(playerId);
    }

    @Override
    public boolean contains(UUID playerId) {
        return cache.containsKey(playerId);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
