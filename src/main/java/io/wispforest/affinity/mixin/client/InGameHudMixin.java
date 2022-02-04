package io.wispforest.affinity.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.render.CrosshairStatProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

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

        final var blockEntity = client.world.getBlockEntity(hit.getBlockPos());
        if (!(blockEntity instanceof CrosshairStatProvider provider)) return;

        var entries = new ArrayList<CrosshairStatProvider.Entry>();
        provider.appendTooltipEntries(entries);

        RenderSystem.disableBlend();
        for (int i = 0; i < entries.size(); i++) {
            CrosshairStatProvider.Entry entry = entries.get(i);

            RenderSystem.setShaderTexture(0, entry.texture());

            DrawableHelper.drawTexture(matrices, this.scaledWidth / 2 + 10, this.scaledHeight / 2 + i * 10, entry.x(), entry.y(), 8, 8, 32, 32);
            client.textRenderer.draw(matrices, entry.text(), this.scaledWidth / 2f + 10 + 15, this.scaledHeight / 2f + i * 10, 0xFFFFFF);
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }

}
