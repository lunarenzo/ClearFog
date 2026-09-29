package de.rapha149.clearfog.listener.player;

import de.rapha149.clearfog.cache.ViewDistanceCache;
import de.rapha149.clearfog.scheduler.TaskScheduler;
import de.rapha149.clearfog.service.FogService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;
import java.util.UUID;

public final class PlayerConnectionListener implements Listener {

    private final FogService fogService;
    private final ViewDistanceCache cache;
    private final TaskScheduler scheduler;

    public PlayerConnectionListener(FogService fogService, ViewDistanceCache cache, TaskScheduler scheduler) {
        this.fogService = Objects.requireNonNull(fogService, "fogService cannot be null");
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler cannot be null");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int target = fogService.calculateEffectiveViewDistance(
                uuid,
                player.getWorld().getName(),
                player.getWorld().getViewDistance()
        );
        cache.put(uuid, target);

        scheduler.runEntity(player, () -> fogService.refreshPlayer(player, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cache.remove(uuid);
    }
}
