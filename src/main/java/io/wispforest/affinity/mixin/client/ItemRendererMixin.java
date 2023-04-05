package io.wispforest.affinity.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.render.AbsoluteEnchantmentGlintHandler;
import io.wispforest.affinity.client.render.PostItemRenderCallback;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private boolean affinity$itemBarRendered = false;

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"))
    private void captureGlintColor(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        AbsoluteEnchantmentGlintHandler.prepareGlintColor(stack);
    }

    @Inject(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void callPostRenderEvent(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        var item = MixinHooks.RENDER_ITEM != null && MixinHooks.RENDER_ITEM.present()
                ? MixinHooks.RENDER_ITEM.get()
                : null;

        PostItemRenderCallback.EVENT.invoker().postRender(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, item);

        MixinHooks.RENDER_ITEM = null;
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void resetItemBarState(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        this.affinity$itemBarRendered = false;
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 0))
    private void injectSecondaryItemBar(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        this.affinity$itemBarRendered = true;
        this.affinity$renderSecondaryBar(matrices, x + 2, y + 11, stack);
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", opcode = Opcodes.GETFIELD))
    private void injectLateSecondaryItemBar(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (this.affinity$itemBarRendered) return;

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        this.affinity$renderSecondaryBar(matrices, x + 2, y + 13, stack);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
    }

    @Unique
    private void affinity$renderSecondaryBar(MatrixStack matrices, int x, int y, ItemStack stack) {
        if (!(stack.getItem() instanceof ArtifactBladeItem blade)) return;

        int abilityTicks = ArtifactBladeItem.getAbilityTicks(MinecraftClient.getInstance().world, stack);
        if (abilityTicks < 0) return;

        int progress = 13 - Math.round((abilityTicks / (float) blade.abilityDuration()) * 13);
        int color = 0xFF0096FF;

        Drawer.fill(matrices, x, y, x + 13, y + 2, 0xFF000000);
        Drawer.fill(matrices, x, y, x + progress, y + 1, color);
    }
}
