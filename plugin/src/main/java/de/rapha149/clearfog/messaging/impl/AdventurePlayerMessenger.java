package de.rapha149.clearfog.messaging.impl;

import de.rapha149.clearfog.messaging.PlayerMessenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public final class AdventurePlayerMessenger implements PlayerMessenger {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void send(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(message));
        }
    }

    @Override
    public void send(CommandSender sender, Component component) {
        if (sender != null && component != null) {
            sender.sendMessage(component);
        }
    }
}
