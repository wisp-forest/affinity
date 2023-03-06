package io.wispforest.affinity.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import io.wispforest.affinity.client.render.PostItemRenderCallback;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    protected abstract void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    private boolean affinity$itemBarRendered = false;

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"))
    private void captureGlintColor(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        AbsoluteEnchantmentGlintHandler.prepareGlintColor(stack);
    }

    @Inject(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void callPostRenderEvent(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        var item = MixinHooks.RENDER_ITEM != null && MixinHooks.RENDER_ITEM.present()
                ? MixinHooks.RENDER_ITEM.get()
                : null;

        PostItemRenderCallback.EVENT.invoker().postRender(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, item);

        MixinHooks.RENDER_ITEM = null;
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void resetItemBarState(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        this.affinity$itemBarRendered = false;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiQuad(Lnet/minecraft/client/render/BufferBuilder;IIIIIIII)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectSecondaryItemBar(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci, Tessellator tessellator, BufferBuilder bufferBuilder) {
        this.affinity$itemBarRendered = true;
        this.affinity$renderSecondaryBar(x + 2, y + 11, stack, bufferBuilder);
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", opcode = Opcodes.GETFIELD))
    private void injectSecondaryItemBar(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (this.affinity$itemBarRendered) return;

        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        this.affinity$renderSecondaryBar(x + 2, y + 13, stack, Tessellator.getInstance().getBuffer());
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    @Unique
    private void affinity$renderSecondaryBar(int x, int y, ItemStack stack, BufferBuilder buffer) {
        if (!(stack.getItem() instanceof ArtifactBladeItem blade)) return;

        int abilityTicks = ArtifactBladeItem.getAbilityTicks(MinecraftClient.getInstance().world, stack);
        if (abilityTicks < 0) return;

        int progress = 13 - Math.round((abilityTicks / (float) blade.abilityDuration()) * 13);
        int color = 0x0096FF;

        this.renderGuiQuad(buffer, x, y, 13, 2, 0, 0, 0, 255);
        this.renderGuiQuad(buffer, x, y, progress, 1, color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 255);
    }
}
