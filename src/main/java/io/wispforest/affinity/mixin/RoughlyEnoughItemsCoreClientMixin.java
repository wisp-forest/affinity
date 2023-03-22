package io.wispforest.affinity.mixin;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.shedaniel.rei.RoughlyEnoughItemsCoreClient", remap = false)
public class RoughlyEnoughItemsCoreClientMixin {

    private Screen owo$theUnscreenifiedScreen = null;

    @Inject(method = "lambda$registerEvents$22", at = @At("HEAD"), cancellable = true)
    private void skipEarlyRender(int[] rendered, HandledScreen<?> screen, MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(screen instanceof BaseOwoHandledScreen<?, ?>)) return;
        ci.cancel();
    }

    @Inject(method = "lambda$registerEvents$23", at = @At("HEAD"), cancellable = true)
    private void skipNotQuiteSoEarlyRender(int[] rendered, HandledScreen<?> screen, MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(screen instanceof BaseOwoHandledScreen<?, ?>)) return;
        ci.cancel();
    }

    @ModifyVariable(method = "lambda$registerEvents$24", at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPNE, ordinal = 0), argsOnly = true)
    private Screen unScreenifyTheScreen(Screen screen) {
        if (!(screen instanceof BaseOwoHandledScreen<?, ?>)) return screen;

        this.owo$theUnscreenifiedScreen = screen;
        return null;
    }

    @ModifyVariable(method = "lambda$registerEvents$24", at = @At(value = "INVOKE", target = "Lme/shedaniel/rei/RoughlyEnoughItemsCoreClient;resetFocused(Lnet/minecraft/client/gui/screen/Screen;)Z", shift = At.Shift.BEFORE), argsOnly = true)
    private Screen reScreenifyTheScreen(Screen screen) {
        if (this.owo$theUnscreenifiedScreen == null) return screen;

        var thatScreen = this.owo$theUnscreenifiedScreen;
        this.owo$theUnscreenifiedScreen = null;

        return thatScreen;
    }

}
