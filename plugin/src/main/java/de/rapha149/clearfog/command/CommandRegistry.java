package de.rapha149.clearfog.command;

import de.rapha149.clearfog.command.impl.FogCommandHandler;
import de.rapha149.clearfog.config.ConfigManager;
import de.rapha149.clearfog.messaging.PlayerMessenger;
import de.rapha149.clearfog.service.FogService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class CommandRegistry {

    private final JavaPlugin plugin;
    private final FogService fogService;
    private final ConfigManager configManager;
    private final PlayerMessenger messenger;

    public CommandRegistry(JavaPlugin plugin, FogService fogService, ConfigManager configManager, PlayerMessenger messenger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.fogService = Objects.requireNonNull(fogService, "fogService cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.messenger = Objects.requireNonNull(messenger, "messenger cannot be null");
    }

    public void registerAll() {
        FogCommandHandler handler = new FogCommandHandler(fogService, configManager, messenger);
        try {
            PluginCommand cmd = plugin.getCommand("fog");
            if (cmd != null) {
                cmd.setExecutor(handler);
                cmd.setTabCompleter(handler);
                return;
            }
        } catch (UnsupportedOperationException ignored) {
            // Paper plugin loader environment where YAML-based commands are disallowed
        }

        Command fallbackCommand = new Command("fog", "Manage the ClearFog plugin.", "/fog", List.of("worldfog", "myfog")) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return handler.onCommand(sender, this, commandLabel, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                return handler.onTabComplete(sender, this, alias, args);
            }
        };
        fallbackCommand.setPermission("clearfog");
        plugin.getServer().getCommandMap().register(plugin.getName().toLowerCase(), fallbackCommand);
    }
}
