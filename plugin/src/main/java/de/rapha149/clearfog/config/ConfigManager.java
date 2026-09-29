package de.rapha149.clearfog.config;

import de.rapha149.clearfog.scheduler.TaskScheduler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public final class ConfigManager {

    private final Plugin plugin;
    private final TaskScheduler scheduler;
    private final Logger logger;
    private volatile FogConfig currentConfig;

    public ConfigManager(Plugin plugin, TaskScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler cannot be null");
        this.logger = plugin.getLogger();
    }

    public FogConfig load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        config.addDefault("check-for-updates", true);
        config.addDefault("direct-view-distance-updates", false);
        config.addDefault("default.enabled", true);
        config.addDefault("default.view-distance", 32);
        config.addDefault("world.enabled", false);
        if (!config.isConfigurationSection("world.worlds")) {
            config.createSection("world.worlds");
        }
        config.addDefault("individual.enabled", false);
        if (!config.isConfigurationSection("individual.players")) {
            config.createSection("individual.players");
        }
        config.options().copyDefaults(true);
        plugin.saveConfig();

        boolean checkForUpdates = config.getBoolean("check-for-updates", true);
        boolean directUpdates = config.getBoolean("direct-view-distance-updates", false);
        boolean defaultEnabled = config.getBoolean("default.enabled", true);
        int defaultViewDistance = Math.max(1, config.getInt("default.view-distance", 32));

        boolean worldEnabled = config.getBoolean("world.enabled", false);
        Map<String, Integer> worlds = new HashMap<>();
        ConfigurationSection worldSec = config.getConfigurationSection("world.worlds");
        if (worldSec != null) {
            for (String w : worldSec.getKeys(false)) {
                int dist = worldSec.getInt(w);
                if (dist >= 1) {
                    worlds.put(w, dist);
                } else {
                    logger.warning("Invalid view distance for world '" + w + "': " + dist);
                }
            }
        }

        boolean individualEnabled = config.getBoolean("individual.enabled", false);
        Map<UUID, Integer> individuals = new HashMap<>();
        ConfigurationSection indSec = config.getConfigurationSection("individual.players");
        if (indSec != null) {
            for (String uStr : indSec.getKeys(false)) {
                try {
                    UUID u = UUID.fromString(uStr);
                    int dist = indSec.getInt(uStr);
                    if (dist >= 1) {
                        individuals.put(u, dist);
                    } else {
                        logger.warning("Invalid individual view distance for '" + uStr + "': " + dist);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid UUID in individual.players: " + uStr);
                }
            }
        }

        FogConfig fogConfig = new FogConfig(
                checkForUpdates,
                directUpdates,
                defaultEnabled,
                defaultViewDistance,
                worldEnabled,
                worlds,
                individualEnabled,
                individuals
        );
        this.currentConfig = fogConfig;
        return fogConfig;
    }

    public FogConfig getCurrentConfig() {
        FogConfig cfg = this.currentConfig;
        return cfg != null ? cfg : FogConfig.createDefault();
    }

    public void saveAsync(FogConfig newConfig) {
        this.currentConfig = newConfig;
        scheduler.runAsync(() -> {
            synchronized (this) {
                FileConfiguration config = plugin.getConfig();
                config.set("check-for-updates", newConfig.checkForUpdates());
                config.set("direct-view-distance-updates", newConfig.directUpdates());
                config.set("default.enabled", newConfig.defaultEnabled());
                config.set("default.view-distance", newConfig.defaultViewDistance());
                config.set("world.enabled", newConfig.worldEnabled());

                config.set("world.worlds", null);
                for (Map.Entry<String, Integer> entry : newConfig.worldViewDistances().entrySet()) {
                    config.set("world.worlds." + entry.getKey(), entry.getValue());
                }

                config.set("individual.enabled", newConfig.individualEnabled());
                config.set("individual.players", null);
                for (Map.Entry<UUID, Integer> entry : newConfig.individualViewDistances().entrySet()) {
                    config.set("individual.players." + entry.getKey(), entry.getValue());
                }

                plugin.saveConfig();
            }
        });
    }
}
