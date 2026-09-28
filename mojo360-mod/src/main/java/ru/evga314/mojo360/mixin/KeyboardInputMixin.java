package ru.evga314.mojo360.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.evga314.mojo360.AnalogInput;
import ru.evga314.mojo360.Mojo360Client;

// extends ClientInput -> доступ к protected moveVector
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    // после ванили: она уже собрала WASD, мы подменяем если джойстик отклонён
    @Inject(method = "tick", at = @At("TAIL"))
    private void mojo360$analogMove(CallbackInfo ci) {
        Vec2 move = AnalogInput.pollMove();
        if (move == null) return;
        Vec2 vanilla = this.moveVector;
        this.moveVector = move;
        this.keyPresses = AnalogInput.keysFor(move, this.keyPresses);
        Mojo360Client.debugRate("out", "vec {} -> ({}, {}) keys {}",
                vanilla, move.x, move.y, this.keyPresses);
    }
}
