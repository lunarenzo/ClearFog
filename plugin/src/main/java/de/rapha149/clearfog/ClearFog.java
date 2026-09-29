package de.rapha149.clearfog;

import de.rapha149.clearfog.cache.ViewDistanceCache;
import de.rapha149.clearfog.cache.impl.ConcurrentViewDistanceCache;
import de.rapha149.clearfog.command.CommandRegistry;
import de.rapha149.clearfog.config.ConfigManager;
import de.rapha149.clearfog.config.FogConfig;
import de.rapha149.clearfog.listener.ListenerRegistry;
import de.rapha149.clearfog.messaging.PlayerMessenger;
import de.rapha149.clearfog.messaging.impl.AdventurePlayerMessenger;
import de.rapha149.clearfog.network.NetworkInjector;
import de.rapha149.clearfog.network.impl.NettyNetworkInjector;
import de.rapha149.clearfog.scheduler.TaskScheduler;
import de.rapha149.clearfog.service.FogService;
import de.rapha149.clearfog.service.impl.DefaultFogService;
import de.rapha149.clearfog.version.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class ClearFog extends JavaPlugin {

    private static final Map<String, String> VERSIONS = Map.ofEntries(
            Map.entry("1.20.5", "1_20_R4"),
            Map.entry("1.20.6", "1_20_R4"),
            Map.entry("1.21.1", "1_21_R1"),
            Map.entry("1.21.3", "1_21_R2"),
            Map.entry("1.21.4", "1_21_R3"),
            Map.entry("1.21.5", "1_21_R4"),
            Map.entry("1.21.6", "1_21_R5"),
            Map.entry("1.21.7", "1_21_R5"),
            Map.entry("1.21.8", "1_21_R5"),
            Map.entry("1.21.10", "1_21_R6"),
            Map.entry("1.21.11", "1_21_R7"),
            Map.entry("26.1", "26_R1"),
            Map.entry("26.1.1", "26_R1"),
            Map.entry("26.1.2", "26_R1"),
            Map.entry("26.2", "26_R2")
    );
    private static final String NEWEST_VERSION = "26_R2";

    private TaskScheduler taskScheduler;
    private ConfigManager configManager;
    private ViewDistanceCache viewDistanceCache;
    private FogService fogService;
    private NetworkInjector networkInjector;
    private ListenerRegistry listenerRegistry;
    private CommandRegistry commandRegistry;
    private PlayerMessenger playerMessenger;

    @Override
    public void onEnable() {
        // 1. Version Detection & Wrapper Instantiation
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion;
        if (craftBukkitPackage.contains(".v")) {
            nmsVersion = craftBukkitPackage.split("\\.")[3].substring(1);
        } else {
            String bukkitVersion = Bukkit.getBukkitVersion().split("-")[0];
            int buildIndex = bukkitVersion.indexOf(".build.");
            if (buildIndex != -1) {
                bukkitVersion = bukkitVersion.substring(0, buildIndex);
            }
            nmsVersion = VERSIONS.getOrDefault(bukkitVersion, NEWEST_VERSION);
        }
        getLogger().info("Server version \"" + Bukkit.getBukkitVersion() + "\" detected, using version support \"" + nmsVersion + "\".");

        VersionWrapper wrapper;
        try {
            Class<?> wrapperClass = Class.forName(VersionWrapper.class.getPackage().getName() + ".Wrapper" + nmsVersion);
            wrapper = (VersionWrapper) wrapperClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to load ClearFog support for server version \"" + nmsVersion + "\"", e);
        }

        // 2. Scheduler & Configuration
        this.taskScheduler = new TaskScheduler(this);
        this.configManager = new ConfigManager(this, this.taskScheduler);
        FogConfig config = this.configManager.load();

        // 3. Lock-free Cache & Services
        this.viewDistanceCache = new ConcurrentViewDistanceCache();
        this.fogService = new DefaultFogService(this.viewDistanceCache, wrapper, config);
        this.playerMessenger = new AdventurePlayerMessenger();

        // 4. Netty Pipeline Injection
        this.networkInjector = new NettyNetworkInjector(this.viewDistanceCache, this.fogService, wrapper);
        try {
            this.networkInjector.registerServerPipelines();
        } catch (Exception e) {
            getLogger().severe("Failed to inject ClearFog into Netty server pipelines!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 5. Presentation, Listeners & Commands
        this.listenerRegistry = new ListenerRegistry(this, this.fogService, this.viewDistanceCache, this.taskScheduler);
        this.listenerRegistry.registerAll();

        this.commandRegistry = new CommandRegistry(this, this.fogService, this.configManager, this.playerMessenger);
        this.commandRegistry.registerAll();

        getLogger().info("ClearFog successfully enabled.");
    }

    @Override
    public void onDisable() {
        if (this.listenerRegistry != null) {
            this.listenerRegistry.unregisterAll();
        }

        if (this.networkInjector != null) {
            try {
                this.networkInjector.unregisterServerPipelines();
            } catch (Exception e) {
                getLogger().warning("Failed to cleanly unregister Netty server pipelines on shutdown.");
            }
        }

        if (this.viewDistanceCache != null) {
            this.viewDistanceCache.clear();
        }

        getLogger().info("ClearFog disabled.");
    }
}
