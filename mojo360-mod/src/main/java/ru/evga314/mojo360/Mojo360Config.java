package ru.evga314.mojo360;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fabricmc.loader.api.FabricLoader;

/** config/mojo360.json, три флажка. */
public final class Mojo360Config {
    private static volatile boolean enabled = true;
    private static volatile boolean debug = false;
    // false = всегда полная скорость, от джойстика берём только направление
    private static volatile boolean useStrength = true;

    private Mojo360Config() {
    }

    public static void load() {
        Path path = getPath();
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                enabled = readBool(json, "enabled", true);
                debug = readBool(json, "debug", false);
                useStrength = readBool(json, "use_strength", true);
            }
        } catch (IOException | RuntimeException e) {
            Mojo360Client.LOGGER.warn("[mojo360] bad config, using defaults: {}", e.toString());
        }
        save(); // допишет новые ключи
    }

    private static boolean readBool(String json, String key, boolean def) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)").matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : def;
    }

    public static void save() {
        Path path = getPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "{\n"
                    + "  \"enabled\": " + enabled + ",\n"
                    + "  \"use_strength\": " + useStrength + ",\n"
                    + "  \"debug\": " + debug + "\n"
                    + "}\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            Mojo360Client.LOGGER.warn("[mojo360] config save failed: {}", e.toString());
        }
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("mojo360.json");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean value) {
        debug = value;
    }

    public static boolean isUseStrength() {
        return useStrength;
    }

    public static void setUseStrength(boolean value) {
        useStrength = value;
    }
}
