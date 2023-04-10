package io.wispforest.affinity.mixin.client;

import dev.emi.trinkets.api.TrinketsApi;
import io.wispforest.affinity.client.render.blockentity.AethumFluxNodeBlockEntityRenderer;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render", at = @At("HEAD"))
    private void disableLinkRendering(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        AethumFluxNodeBlockEntityRenderer.enableLinkRendering = false;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void enableLinkRendering(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        AethumFluxNodeBlockEntityRenderer.enableLinkRendering = true;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @ModifyVariable(method = "renderStatusBars", at = @At("LOAD"))
    private int setPlayerHealth(int health) {
        if (TrinketsApi.getTrinketComponent(getCameraPlayer()).get().isEquipped(AffinityItems.BLACK_HEART_RING))
            return 0;

        return health;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @ModifyConstant(method = "renderHealthBar", constant = @Constant(intValue = 4))
    private int makeTheHeartsNotJiggle(int maxHealth) {
        if (TrinketsApi.getTrinketComponent(getCameraPlayer()).get().isEquipped(AffinityItems.BLACK_HEART_RING))
            return -1;

        return maxHealth;
    }
}
