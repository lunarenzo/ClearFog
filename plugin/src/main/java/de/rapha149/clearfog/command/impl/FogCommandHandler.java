package de.rapha149.clearfog.command.impl;

import de.rapha149.clearfog.config.ConfigManager;
import de.rapha149.clearfog.config.FogConfig;
import de.rapha149.clearfog.constant.Messages;
import de.rapha149.clearfog.constant.Permissions;
import de.rapha149.clearfog.messaging.PlayerMessenger;
import de.rapha149.clearfog.service.FogService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public final class FogCommandHandler implements CommandExecutor, TabCompleter {

    private final FogService fogService;
    private final ConfigManager configManager;
    private final PlayerMessenger messenger;

    public FogCommandHandler(FogService fogService, ConfigManager configManager, PlayerMessenger messenger) {
        this.fogService = Objects.requireNonNull(fogService, "fogService cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.messenger = Objects.requireNonNull(messenger, "messenger cannot be null");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(Permissions.BASE)) {
            messenger.send(sender, Messages.NO_PERMISSION);
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "";

        if (alias.equalsIgnoreCase("worldfog")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "world";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return onCommand(sender, command, "fog", newArgs);
        }

        if (alias.equalsIgnoreCase("myfog")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "individual";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return onCommand(sender, command, "fog", newArgs);
        }

        switch (sub) {
            case "reload":
                return handleReload(sender);
            case "default":
                return handleDefault(sender, args);
            case "world":
                return handleWorld(sender, args);
            case "individual":
                return handleIndividual(sender, args);
            default:
                messenger.send(sender, Messages.PREFIX + "<gray>Usage: /" + alias + " <reload|default|world|individual></gray>");
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(Permissions.RELOAD)) {
            messenger.send(sender, Messages.NO_PERMISSION);
            return true;
        }
        FogConfig reloaded = configManager.load();
        fogService.updateConfig(reloaded);
        fogService.refreshAll(reloaded.directUpdates());
        messenger.send(sender, Messages.RELOAD_SUCCESS);
        return true;
    }

    private boolean handleDefault(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.DEFAULT)) {
            messenger.send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length < 2) {
            messenger.send(sender, Messages.PREFIX + "<gray>Usage: /fog default <get|set <distance>></gray>");
            return true;
        }
        String action = args[1].toLowerCase();
        FogConfig cfg = fogService.getConfig();

        if (action.equals("get")) {
            messenger.send(sender, Messages.VALUE_CURRENT
                    .replace("<target>", "Default")
                    .replace("<distance>", String.valueOf(cfg.defaultViewDistance())));
            return true;
        } else if (action.equals("set") && args.length >= 3) {
            try {
                int dist = Integer.parseInt(args[2]);
                if (dist < 1) {
                    messenger.send(sender, Messages.VALUE_OUT_OF_BOUNDS);
                    return true;
                }
                FogConfig updated = new FogConfig(
                        cfg.checkForUpdates(),
                        cfg.directUpdates(),
                        cfg.defaultEnabled(),
                        dist,
                        cfg.worldEnabled(),
                        cfg.worldViewDistances(),
                        cfg.individualEnabled(),
                        cfg.individualViewDistances()
                );
                configManager.saveAsync(updated);
                fogService.updateConfig(updated);
                fogService.refreshAll(updated.directUpdates());
                messenger.send(sender, Messages.VALUE_SET_SUCCESS
                        .replace("<target>", "Default")
                        .replace("<distance>", String.valueOf(dist)));
            } catch (NumberFormatException e) {
                messenger.send(sender, Messages.INVALID_NUMBER);
            }
            return true;
        }
        return true;
    }

    private boolean handleWorld(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.WORLD)) {
            messenger.send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length < 2) {
            messenger.send(sender, Messages.PREFIX + "<gray>Usage: /fog world <get|set|unset|list> [world] [distance]</gray>");
            return true;
        }
        String action = args[1].toLowerCase();
        FogConfig cfg = fogService.getConfig();

        if (action.equals("list")) {
            messenger.send(sender, Messages.LIST_HEADER.replace("<target>", "World"));
            if (cfg.worldViewDistances().isEmpty()) {
                messenger.send(sender, Messages.LIST_EMPTY);
            } else {
                for (Map.Entry<String, Integer> e : cfg.worldViewDistances().entrySet()) {
                    messenger.send(sender, Messages.LIST_ENTRY
                            .replace("<name>", e.getKey())
                            .replace("<distance>", String.valueOf(e.getValue())));
                }
            }
            return true;
        }

        String worldName = args.length >= 3 ? args[2] : (sender instanceof Player p ? p.getWorld().getName() : null);
        if (worldName == null) {
            messenger.send(sender, Messages.PREFIX + "<red>Specify a world name.</red>");
            return true;
        }

        if (action.equals("get")) {
            Integer dist = cfg.worldViewDistances().get(worldName);
            if (dist != null) {
                messenger.send(sender, Messages.VALUE_CURRENT
                        .replace("<target>", "World " + worldName)
                        .replace("<distance>", String.valueOf(dist)));
            } else {
                messenger.send(sender, Messages.VALUE_NOT_SET.replace("<target>", "World " + worldName));
            }
            return true;
        } else if (action.equals("set") && args.length >= 4) {
            try {
                int dist = Integer.parseInt(args[3]);
                if (dist < 1) {
                    messenger.send(sender, Messages.VALUE_OUT_OF_BOUNDS);
                    return true;
                }
                Map<String, Integer> map = new HashMap<>(cfg.worldViewDistances());
                map.put(worldName, dist);
                FogConfig updated = new FogConfig(
                        cfg.checkForUpdates(), cfg.directUpdates(), cfg.defaultEnabled(), cfg.defaultViewDistance(),
                        true, map, cfg.individualEnabled(), cfg.individualViewDistances()
                );
                configManager.saveAsync(updated);
                fogService.updateConfig(updated);
                World w = Bukkit.getWorld(worldName);
                if (w != null) {
                    fogService.refreshWorld(w, updated.directUpdates());
                }
                messenger.send(sender, Messages.VALUE_SET_SUCCESS
                        .replace("<target>", "World " + worldName)
                        .replace("<distance>", String.valueOf(dist)));
            } catch (NumberFormatException e) {
                messenger.send(sender, Messages.INVALID_NUMBER);
            }
            return true;
        } else if (action.equals("unset")) {
            Map<String, Integer> map = new HashMap<>(cfg.worldViewDistances());
            map.remove(worldName);
            FogConfig updated = new FogConfig(
                    cfg.checkForUpdates(), cfg.directUpdates(), cfg.defaultEnabled(), cfg.defaultViewDistance(),
                    cfg.worldEnabled(), map, cfg.individualEnabled(), cfg.individualViewDistances()
            );
            configManager.saveAsync(updated);
            fogService.updateConfig(updated);
            World w = Bukkit.getWorld(worldName);
            if (w != null) {
                fogService.refreshWorld(w, updated.directUpdates());
            }
            messenger.send(sender, Messages.VALUE_UNSET_SUCCESS.replace("<target>", "World " + worldName));
            return true;
        }
        return true;
    }

    private boolean handleIndividual(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.INDIVIDUAL)) {
            messenger.send(sender, Messages.NO_PERMISSION);
            return true;
        }
        if (args.length < 2) {
            messenger.send(sender, Messages.PREFIX + "<gray>Usage: /fog individual <get|set|unset|list> [player] [distance]</gray>");
            return true;
        }
        String action = args[1].toLowerCase();
        FogConfig cfg = fogService.getConfig();

        if (action.equals("list")) {
            messenger.send(sender, Messages.LIST_HEADER.replace("<target>", "Player"));
            if (cfg.individualViewDistances().isEmpty()) {
                messenger.send(sender, Messages.LIST_EMPTY);
            } else {
                for (Map.Entry<UUID, Integer> e : cfg.individualViewDistances().entrySet()) {
                    Player p = Bukkit.getPlayer(e.getKey());
                    String name = p != null ? p.getName() : e.getKey().toString();
                    messenger.send(sender, Messages.LIST_ENTRY
                            .replace("<name>", name)
                            .replace("<distance>", String.valueOf(e.getValue())));
                }
            }
            return true;
        }

        Player target = args.length >= 3 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messenger.send(sender, Messages.PLAYER_NOT_FOUND.replace("<player>", args.length >= 3 ? args[2] : ""));
            return true;
        }

        UUID uuid = target.getUniqueId();
        if (action.equals("get")) {
            Integer dist = cfg.individualViewDistances().get(uuid);
            if (dist != null) {
                messenger.send(sender, Messages.VALUE_CURRENT
                        .replace("<target>", target.getName())
                        .replace("<distance>", String.valueOf(dist)));
            } else {
                messenger.send(sender, Messages.VALUE_NOT_SET.replace("<target>", target.getName()));
            }
            return true;
        } else if (action.equals("set") && args.length >= 4) {
            try {
                int dist = Integer.parseInt(args[3]);
                if (dist < 1) {
                    messenger.send(sender, Messages.VALUE_OUT_OF_BOUNDS);
                    return true;
                }
                Map<UUID, Integer> map = new HashMap<>(cfg.individualViewDistances());
                map.put(uuid, dist);
                FogConfig updated = new FogConfig(
                        cfg.checkForUpdates(), cfg.directUpdates(), cfg.defaultEnabled(), cfg.defaultViewDistance(),
                        cfg.worldEnabled(), cfg.worldViewDistances(), true, map
                );
                configManager.saveAsync(updated);
                fogService.updateConfig(updated);
                fogService.refreshPlayer(target, updated.directUpdates());
                messenger.send(sender, Messages.VALUE_SET_SUCCESS
                        .replace("<target>", target.getName())
                        .replace("<distance>", String.valueOf(dist)));
            } catch (NumberFormatException e) {
                messenger.send(sender, Messages.INVALID_NUMBER);
            }
            return true;
        } else if (action.equals("unset")) {
            Map<UUID, Integer> map = new HashMap<>(cfg.individualViewDistances());
            map.remove(uuid);
            FogConfig updated = new FogConfig(
                    cfg.checkForUpdates(), cfg.directUpdates(), cfg.defaultEnabled(), cfg.defaultViewDistance(),
                    cfg.worldEnabled(), cfg.worldViewDistances(), cfg.individualEnabled(), map
            );
            configManager.saveAsync(updated);
            fogService.updateConfig(updated);
            fogService.refreshPlayer(target, updated.directUpdates());
            messenger.send(sender, Messages.VALUE_UNSET_SUCCESS.replace("<target>", target.getName()));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("reload", "default", "world", "individual"), args[0]);
        }
        if (args.length == 2) {
            return filter(List.of("get", "set", "unset", "list"), args[1]);
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("world")) {
                return filter(Bukkit.getWorlds().stream().map(World::getName).toList(), args[2]);
            }
            if (sub.equals("individual")) {
                // FIXED: Use online players ONLY to avoid blocking disk I/O!
                return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[2]);
            }
        }
        return List.of();
    }

    private List<String> filter(Collection<String> items, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (item.toLowerCase().startsWith(lower)) {
                result.add(item);
            }
        }
        return result;
    }
}
