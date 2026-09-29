package de.rapha149.clearfog.network;

import org.bukkit.entity.Player;

public interface NetworkInjector {

    void registerServerPipelines() throws Exception;

    void unregisterServerPipelines() throws Exception;

    void uninjectPlayer(Player player);
}
