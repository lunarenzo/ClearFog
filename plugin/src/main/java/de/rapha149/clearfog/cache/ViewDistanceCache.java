package de.rapha149.clearfog.cache;

import java.util.UUID;

public interface ViewDistanceCache {

    int get(UUID playerId);

    void put(UUID playerId, int viewDistance);

    void remove(UUID playerId);

    boolean contains(UUID playerId);

    void clear();
}
