package ru.evga314.mojo360.bridge;

import java.io.File;

import ru.evga314.mojo360.Mojo360Client;

/**
 * Связь с libpojavexec MojoLauncher (analog_bridge.c).
 * Имя класса и методов = имена JNI-символов, не переименовывать.
 * Нет либы / нет метода -> молча ничего не делаем (не Mojo или старый Mojo).
 */
public final class AnalogBridge {
    public enum State {
        NOT_TRIED,   // ещё не грузили
        NO_LIBRARY,  // libpojavexec не найдена (ПК, другой лаунчер)
        NO_METHOD,   // либа есть, метода нет (лаунчер без поддержки)
        NO_LAUNCHER, // метод есть, но лаунчер не отметился -> другой экземпляр либы
        READY        // рукопожатие прошло
    }

    private static final String LIB = "pojavexec";

    private static State state = State.NOT_TRIED;
    private static boolean loadTried = false;
    private static boolean loaded = false;
    private static int apiVersion = 0;
    private static volatile boolean registered = false;

    private AnalogBridge() {
    }

    private static native int registerAnalogMovement();

    private static native void unregisterAnalogMovement();

    private static native long pollAnalogMovement();

    /** Вкл/выкл флаг в нативной памяти. Можно звать много раз. */
    public static synchronized void setEnabled(boolean on) {
        if (on) register();
        else unregister();
    }

    private static void register() {
        if (registered) return;
        if (!ensureLoaded()) return;
        try {
            apiVersion = registerAnalogMovement();
        } catch (UnsatisfiedLinkError e) {
            state = State.NO_METHOD;
            Mojo360Client.LOGGER.info("[mojo360] handshake: launcher has no analog bridge, idle");
            return;
        }
        if (apiVersion <= 0) {
            state = State.NO_LAUNCHER;
            Mojo360Client.LOGGER.warn("[mojo360] handshake: launcher side not visible (separate lib instance?), idle");
            return;
        }
        state = State.READY;
        registered = true;
        Mojo360Client.LOGGER.info("[mojo360] handshake ok, api v{}, joystick -> analog", apiVersion);
    }

    private static void unregister() {
        if (!registered) return;
        registered = false;
        try {
            unregisterAnalogMovement();
        } catch (UnsatisfiedLinkError ignored) {
            // раз register прошёл, сюда не попадём
        }
        Mojo360Client.LOGGER.info("[mojo360] unregistered, joystick -> WASD");
    }

    /** Грузим libpojavexec в загрузчик этого класса, иначе native не свяжутся. */
    private static boolean ensureLoaded() {
        if (loadTried) return loaded;
        loadTried = true;
        try {
            System.loadLibrary(LIB);
            Mojo360Client.debug("lib loaded via java.library.path");
            return loaded = true;
        } catch (UnsatisfiedLinkError | SecurityException e) {
            Mojo360Client.debug("loadLibrary failed: {}", e.getMessage());
        }
        // запасной путь: каталог либ лаунчера есть в LD_LIBRARY_PATH игры
        String ldPath = System.getenv("LD_LIBRARY_PATH");
        if (ldPath != null) {
            for (String dir : ldPath.split(":")) {
                if (dir.isEmpty()) continue;
                File lib = new File(dir, System.mapLibraryName(LIB));
                if (!lib.isFile()) continue;
                try {
                    System.load(lib.getAbsolutePath());
                    Mojo360Client.debug("lib loaded from {}", lib);
                    return loaded = true;
                } catch (UnsatisfiedLinkError | SecurityException e) {
                    Mojo360Client.debug("load {} failed: {}", lib, e.getMessage());
                }
            }
        }
        state = State.NO_LIBRARY;
        Mojo360Client.LOGGER.info("[mojo360] handshake: libpojavexec not found (not MojoLauncher), idle");
        return false;
    }

    public static boolean isActive() {
        return registered;
    }

    public static State getState() {
        return state;
    }

    public static int getApiVersion() {
        return apiVersion;
    }

    /** x: вправо +, y: вперёд +. Упаковку см. pack_xy в analog_bridge.c. */
    public static float[] poll(float[] out) {
        long packed = pollAnalogMovement();
        out[0] = Float.intBitsToFloat((int) (packed >>> 32));
        out[1] = Float.intBitsToFloat((int) packed);
        return out;
    }
}
