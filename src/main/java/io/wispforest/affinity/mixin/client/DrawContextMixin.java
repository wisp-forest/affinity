package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow
    public abstract void fill(RenderLayer layer, int x1, int x2, int y1, int y2, int color);

    @Unique
    private boolean itemBarRendered = false;

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void resetItemBarState(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        this.itemBarRendered = false;
    }

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(Lnet/minecraft/client/render/RenderLayer;IIIII)V", ordinal = 0))
    private void injectSecondaryItemBar(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        this.itemBarRendered = true;
        this.affinity$renderSecondaryBar(x + 2, y + 11, stack);
    }

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", opcode = Opcodes.GETFIELD))
    private void injectLateSecondaryItemBar(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (this.itemBarRendered) return;
        this.affinity$renderSecondaryBar(x + 2, y + 13, stack);
    }

    @Unique
    private void affinity$renderSecondaryBar(int x, int y, ItemStack stack) {
        if (!(stack.getItem() instanceof ArtifactBladeItem blade)) return;

        int abilityTicks = ArtifactBladeItem.getAbilityTicks(MinecraftClient.getInstance().world, stack);
        if (abilityTicks < 0) return;

        int progress = 13 - Math.round((abilityTicks / (float) blade.abilityDuration()) * 13);
        int color = 0xFF0096FF;

        this.fill(RenderLayer.getGuiOverlay(), x, y, x + 13, y + 2, 0xFF000000);
        this.fill(RenderLayer.getGuiOverlay(), x, y, x + progress, y + 1, color);
    }

    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", at = @At("HEAD"), cancellable = true)
    private void captureTooltips(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, CallbackInfo ci) {
        if (MixinHooks.tooltipConsumer == null) return;

        MixinHooks.tooltipConsumer.accept(components);
        ci.cancel();
    }
}
