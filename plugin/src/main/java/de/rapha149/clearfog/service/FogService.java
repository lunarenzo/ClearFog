package de.rapha149.clearfog.service;

import de.rapha149.clearfog.cache.ViewDistanceCache;
import de.rapha149.clearfog.config.FogConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface FogService {

    int calculateEffectiveViewDistance(UUID playerId, String worldName, int fallbackDistance);

    void refreshPlayer(Player player, boolean directUpdate);

    void refreshAll(boolean directUpdate);

    void refreshWorld(World world, boolean directUpdate);

    ViewDistanceCache getCache();

    FogConfig getConfig();

    void updateConfig(FogConfig config);
}
