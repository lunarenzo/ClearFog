package de.rapha149.clearfog.listener;

import de.rapha149.clearfog.cache.ViewDistanceCache;
import de.rapha149.clearfog.listener.player.PlayerConnectionListener;
import de.rapha149.clearfog.listener.player.PlayerWorldListener;
import de.rapha149.clearfog.scheduler.TaskScheduler;
import de.rapha149.clearfog.service.FogService;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class ListenerRegistry {

    private final Plugin plugin;
    private final FogService fogService;
    private final ViewDistanceCache cache;
    private final TaskScheduler scheduler;

    public ListenerRegistry(Plugin plugin, FogService fogService, ViewDistanceCache cache, TaskScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.fogService = Objects.requireNonNull(fogService, "fogService cannot be null");
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler cannot be null");
    }

    public void registerAll() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(fogService, cache, scheduler), plugin);
        pm.registerEvents(new PlayerWorldListener(fogService, scheduler), plugin);
    }

    public void unregisterAll() {
        HandlerList.unregisterAll(plugin);
    }
}
