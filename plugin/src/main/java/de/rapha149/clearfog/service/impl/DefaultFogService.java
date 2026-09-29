package de.rapha149.clearfog.service.impl;

import de.rapha149.clearfog.cache.ViewDistanceCache;
import de.rapha149.clearfog.config.FogConfig;
import de.rapha149.clearfog.service.FogService;
import de.rapha149.clearfog.version.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class DefaultFogService implements FogService {

    private final ViewDistanceCache cache;
    private final VersionWrapper wrapper;
    private volatile FogConfig config;

    public DefaultFogService(ViewDistanceCache cache, VersionWrapper wrapper, FogConfig config) {
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
        this.wrapper = Objects.requireNonNull(wrapper, "wrapper cannot be null");
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    @Override
    public int calculateEffectiveViewDistance(UUID playerId, String worldName, int fallbackDistance) {
        FogConfig cfg = this.config;
        if (playerId != null && cfg.individualEnabled() && cfg.individualViewDistances().containsKey(playerId)) {
            return Math.max(1, cfg.individualViewDistances().get(playerId));
        }

        if (worldName != null && cfg.worldEnabled() && cfg.worldViewDistances().containsKey(worldName)) {
            return Math.max(1, cfg.worldViewDistances().get(worldName));
        }

        if (cfg.defaultEnabled()) {
            return Math.max(1, cfg.defaultViewDistance());
        }

        return Math.max(1, fallbackDistance);
    }

    @Override
    public void refreshPlayer(Player player, boolean directUpdate) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        int fallback = player.getWorld().getViewDistance();
        int targetDistance = calculateEffectiveViewDistance(uuid, player.getWorld().getName(), fallback);

        if (!cache.contains(uuid) || cache.get(uuid) != targetDistance) {
            wrapper.updateViewDistance(player, targetDistance, directUpdate && config.directUpdates());
            cache.put(uuid, targetDistance);
        }
    }

    @Override
    public void refreshAll(boolean directUpdate) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player, directUpdate);
        }
    }

    @Override
    public void refreshWorld(World world, boolean directUpdate) {
        if (world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            refreshPlayer(player, directUpdate);
        }
    }

    @Override
    public ViewDistanceCache getCache() {
        return cache;
    }

    @Override
    public FogConfig getConfig() {
        return config;
    }

    @Override
    public void updateConfig(FogConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }
}
