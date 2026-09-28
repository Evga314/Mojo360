package ru.evga314.mojo360.modmenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import ru.evga314.mojo360.Mojo360Config;
import ru.evga314.mojo360.bridge.AnalogBridge;

/** Три переключателя, применяются и сохраняются сразу. */
public final class Mojo360ConfigScreen extends Screen {
    private static final int W = 220;
    private final Screen parent;

    public Mojo360ConfigScreen(Screen parent) {
        super(Component.translatable("mojo360.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - W / 2;
        int y = this.height / 4 + 8;

        // вкл/выкл без перезапуска: снимаем/ставим флаг в лаунчере
        this.addRenderableWidget(Button.builder(toggleText("enabled", Mojo360Config.isEnabled()), b -> {
            Mojo360Config.setEnabled(!Mojo360Config.isEnabled());
            AnalogBridge.setEnabled(Mojo360Config.isEnabled());
            Mojo360Config.save();
            b.setMessage(toggleText("enabled", Mojo360Config.isEnabled()));
        }).pos(x, y).size(W, 20).build());

        this.addRenderableWidget(Button.builder(toggleText("use_strength", Mojo360Config.isUseStrength()), b -> {
            Mojo360Config.setUseStrength(!Mojo360Config.isUseStrength());
            Mojo360Config.save();
            b.setMessage(toggleText("use_strength", Mojo360Config.isUseStrength()));
        }).pos(x, y + 24).size(W, 20).build());

        this.addRenderableWidget(Button.builder(toggleText("debug", Mojo360Config.isDebug()), b -> {
            Mojo360Config.setDebug(!Mojo360Config.isDebug());
            Mojo360Config.save();
            b.setMessage(toggleText("debug", Mojo360Config.isDebug()));
        }).pos(x, y + 48).size(W, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .pos(x, this.height - 28).size(W, 20).build());
    }

    private static Component toggleText(String key, boolean on) {
        return Component.translatable("mojo360.config." + key,
                on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private static Component statusText() {
        AnalogBridge.State s = AnalogBridge.getState();
        if (s == AnalogBridge.State.READY && !AnalogBridge.isActive()) {
            return Component.translatable("mojo360.status.off");
        }
        return Component.translatable("mojo360.status." + s.name().toLowerCase(java.util.Locale.ROOT),
                AnalogBridge.getApiVersion());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
        g.centeredText(this.font, statusText(), this.width / 2, this.height / 4 - 8, 0xFFA0A0A0);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
