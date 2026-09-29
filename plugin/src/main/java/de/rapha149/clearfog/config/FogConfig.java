package de.rapha149.clearfog.config;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public record FogConfig(
        boolean checkForUpdates,
        boolean directUpdates,
        boolean defaultEnabled,
        int defaultViewDistance,
        boolean worldEnabled,
        Map<String, Integer> worldViewDistances,
        boolean individualEnabled,
        Map<UUID, Integer> individualViewDistances
) {

    public FogConfig {
        worldViewDistances = Collections.unmodifiableMap(worldViewDistances);
        individualViewDistances = Collections.unmodifiableMap(individualViewDistances);
    }

    public static FogConfig createDefault() {
        return new FogConfig(
                true,
                false,
                true,
                32,
                false,
                Map.of(),
                false,
                Map.of()
        );
    }
}
