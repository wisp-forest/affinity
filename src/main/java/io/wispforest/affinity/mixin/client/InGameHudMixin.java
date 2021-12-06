package io.wispforest.affinity.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
            ordinal = 0))
    private void afterCrosshair(MatrixStack matrices, CallbackInfo ci) {
        final var client = MinecraftClient.getInstance();

        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;

        final var member = Affinity.AETHUM_MEMBER.find(client.world, hit.getBlockPos(), null);
        if (member == null) return;

        client.textRenderer.draw(matrices, Text.of("Stored Flux: " + member.flux()), this.scaledWidth / 2f + 10, this.scaledHeight / 2f, 0xFFFFFF);

        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }

}
