package git.mojo.api;

/**
 * Публичный API 360° движения MojoLauncher (libpojavexec, analog_bridge.c).
 * Любой мод может положить себе копию этого класса: имя пакета/класса/методов = имена JNI-символов,
 * не менять. Логики тут нет специально — копии в разных модах не должны расходиться.
 * Спецификация: docs/analog-movement.md в репозитории лаунчера.
 *
 * Перед вызовами загрузить libpojavexec (System.loadLibrary("pojavexec")) из своего мода.
 * Нет либы / метода -> UnsatisfiedLinkError: не Mojo или лаунчер без поддержки.
 */
public final class AnalogMovement {
    /** Версия API, которую знает эта копия класса. */
    public static final int API_VERSION = 1;

    private AnalogMovement() {
    }

    /**
     * Включить аналог в лаунчере (+1 к счётчику модов).
     * @return версия API лаунчера; 0 = лаунчер не виден, ничего не включено
     */
    public static native int registerAnalogMovement();

    /** Парный к успешному register: -1 к счётчику. На 0 лаунчер возвращается к WASD. */
    public static native void unregisterAnalogMovement();

    /** Текущий вектор джойстика, упакован: см. {@link #x(long)} / {@link #y(long)}. */
    public static native long pollAnalogMovement();

    /** Вправо +, -1..1. */
    public static float x(long packed) {
        return Float.intBitsToFloat((int) (packed >>> 32));
    }

    /** Вперёд +, -1..1. */
    public static float y(long packed) {
        return Float.intBitsToFloat((int) packed);
    }
}
