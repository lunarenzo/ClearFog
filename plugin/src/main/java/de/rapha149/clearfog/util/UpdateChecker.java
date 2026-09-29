package de.rapha149.clearfog.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public final class UpdateChecker {

    public static final String SPIGOT_URL = "https://www.spigotmc.org/resources/clearfog.98448";
    private static final int RESOURCE_ID = 98448;
    private static final long COOLDOWN_MILLIS = 7_200_000L;
    private static String cachedResult;
    private static long lastFetchedTime = 0L;

    private UpdateChecker() {
    }

    public static String getAvailableVersion(String currentVersion, Logger logger, boolean warning) {
        long now = System.currentTimeMillis();
        if (now <= lastFetchedTime + COOLDOWN_MILLIS) {
            return cachedResult;
        }

        try {
            String remoteVersion;
            try {
                URL url = URI.create("https://api.spiget.org/v2/resources/" + RESOURCE_ID + "/versions/latest").toURL();
                URLConnection conn = url.openConnection();
                conn.addRequestProperty("User-Agent", "ClearFog-UpdateChecker");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();

                JsonElement root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
                if (root != null && root.isJsonObject()) {
                    remoteVersion = root.getAsJsonObject().get("name").getAsString();
                } else {
                    throw new IllegalStateException("Response is not a valid JSON object");
                }
            } catch (ConnectException e) {
                if (warning && logger != null) {
                    logger.warning("Spiget API unavailable, attempting fallback to Spigot legacy endpoint.");
                }
                URL url = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).toURL();
                URLConnection conn = url.openConnection();
                conn.addRequestProperty("User-Agent", "ClearFog-UpdateChecker");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    remoteVersion = reader.readLine();
                }
            }

            String result = (remoteVersion != null && compare(currentVersion, remoteVersion) < 0) ? remoteVersion : null;
            lastFetchedTime = now;
            cachedResult = result;
            return result;
        } catch (Exception e) {
            if (logger != null) {
                logger.warning("Failed to check for ClearFog updates: " + e.getMessage());
            }
            return null;
        }
    }

    public static int compare(String version1, String version2) {
        String[] split1 = version1.split("\\.");
        String[] split2 = version2.split("\\.");
        int max = Math.max(split1.length, split2.length);

        for (int i = 0; i < max; i++) {
            int v1 = i < split1.length ? Integer.parseInt(split1[i].replaceAll("\\D.*", "")) : 0;
            int v2 = i < split2.length ? Integer.parseInt(split2[i].replaceAll("\\D.*", "")) : 0;
            int cmp = Integer.compare(v1, v2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
