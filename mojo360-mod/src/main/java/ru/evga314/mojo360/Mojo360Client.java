package ru.evga314.mojo360;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.evga314.mojo360.bridge.AnalogBridge;

public class Mojo360Client implements ClientModInitializer {
    public static final String MOD_ID = "mojo360";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // не чаще раза в N мс на ключ, чтобы не забить лог 20 раз/сек
    private static final long LOG_INTERVAL_MS = 500;
    private static final Map<String, Long> lastLog = new HashMap<>();

    @Override
    public void onInitializeClient() {
        Mojo360Config.load();
        LOGGER.info("[mojo360] start, enabled={}, debug={}", Mojo360Config.isEnabled(), Mojo360Config.isDebug());
        // рукопожатие: ставим флаг в нативной памяти лаунчера
        AnalogBridge.setEnabled(Mojo360Config.isEnabled());
    }

    /** Лог только при включённой отладке. */
    public static void debug(String msg, Object... args) {
        if (Mojo360Config.isDebug()) LOGGER.info("[mojo360] " + msg, args);
    }

    /** Отладочный лог с ограничением частоты по ключу. */
    public static void debugRate(String key, String msg, Object... args) {
        if (!Mojo360Config.isDebug()) return;
        long now = System.currentTimeMillis();
        synchronized (lastLog) {
            Long last = lastLog.get(key);
            if (last != null && now - last < LOG_INTERVAL_MS) return;
            lastLog.put(key, now);
        }
        LOGGER.info("[mojo360] " + msg, args);
    }
}
