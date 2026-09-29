package de.rapha149.clearfog.messaging;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public interface PlayerMessenger {

    void send(CommandSender sender, String message);

    void send(CommandSender sender, Component component);
}
