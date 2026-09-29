package de.rapha149.clearfog.scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class TaskScheduler {

    private final Plugin plugin;
    private final boolean isFolia;

    public TaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        boolean foliaCheck = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            foliaCheck = true;
        } catch (ClassNotFoundException ignored) {
        }
        this.isFolia = foliaCheck;
    }

    public void runEntity(Player player, Runnable task) {
        if (isFolia) {
            player.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runGlobal(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runAsync(Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public boolean isFolia() {
        return isFolia;
    }
}
