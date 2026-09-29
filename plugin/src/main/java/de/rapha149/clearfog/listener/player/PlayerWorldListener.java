package de.rapha149.clearfog.listener.player;

import de.rapha149.clearfog.scheduler.TaskScheduler;
import de.rapha149.clearfog.service.FogService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Objects;

public final class PlayerWorldListener implements Listener {

    private final FogService fogService;
    private final TaskScheduler scheduler;

    public PlayerWorldListener(FogService fogService, TaskScheduler scheduler) {
        this.fogService = Objects.requireNonNull(fogService, "fogService cannot be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler cannot be null");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        scheduler.runEntity(player, () -> fogService.refreshPlayer(player, false));
    }
}
