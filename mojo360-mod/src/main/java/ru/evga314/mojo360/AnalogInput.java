package ru.evga314.mojo360;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

import ru.evga314.mojo360.bridge.AnalogBridge;

/** Джойстик -> moveVector + keyPresses. Зовётся из KeyboardInputMixin каждый тик. */
public final class AnalogInput {
    // |v| ниже -> джойстик отпущен, оставляем ванильный ввод (клавиатура)
    private static final float MIN_LENGTH = 1.0E-3F;
    // sin(22.5°): направление считается нажатой клавишей, как в 8-секторном WASD
    private static final float KEY_THRESHOLD = 0.38F;

    private static final float[] raw = new float[2];
    private static boolean wasMoving = false;

    private AnalogInput() {
    }

    /** null = не трогаем ваниль. */
    public static Vec2 pollMove() {
        if (!AnalogBridge.isActive()) return null;
        // в GUI ваниль отпускает клавиши -> и мы не двигаем
        if (Minecraft.getInstance().gui.screen() != null) return stop("screen open");

        AnalogBridge.poll(raw);
        float x = raw[0], y = raw[1];
        float len = (float) Math.sqrt(x * x + y * y);
        if (!(len > MIN_LENGTH)) return stop("released"); // и NaN тоже

        Mojo360Client.debugRate("in", "in x={} y={} len={}", fmt(x), fmt(y), fmt(len));
        // длина: как есть (макс 1) или всегда 1
        float scale = Mojo360Config.isUseStrength() ? Math.min(len, 1F) / len : 1F / len;
        // у MC x = влево +, у лаунчера x = вправо +
        Vec2 move = new Vec2(-x * scale, y * scale);
        if (!wasMoving) {
            wasMoving = true;
            Mojo360Client.debug("analog start");
        }
        return move;
    }

    private static Vec2 stop(String why) {
        if (wasMoving) {
            wasMoving = false;
            Mojo360Client.debug("analog stop: {}", why);
        }
        return null;
    }

    /**
     * Клавиши по направлению: forward для спринта/лодок/пакета на сервер.
     * Прыжок/присед/спринт оставляем ванильные.
     */
    public static Input keysFor(Vec2 move, Input vanilla) {
        float len = move.length();
        float dx = move.x / len, dy = move.y / len;
        return new Input(
                dy > KEY_THRESHOLD,
                dy < -KEY_THRESHOLD,
                dx > KEY_THRESHOLD,
                dx < -KEY_THRESHOLD,
                vanilla.jump(),
                vanilla.shift(),
                vanilla.sprint());
    }

    static String fmt(float v) {
        return String.format(java.util.Locale.ROOT, "%.3f", v);
    }
}
