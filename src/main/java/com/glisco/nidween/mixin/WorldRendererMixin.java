package com.glisco.nidween.mixin;

import com.glisco.nidween.util.components.NidweenComponents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void capturePlayer(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci, Profiler profiler, boolean bl, Vec3d vec3d, double d, double e, double f, Matrix4f matrix4f, boolean bl2, Frustum frustum, boolean bl4, VertexConsumerProvider.Immediate immediate, Iterator<Entity> var26, Entity entity, VertexConsumerProvider vertexConsumerProvider, OutlineVertexConsumerProvider outlineVertexConsumerProvider) {
        if (!(entity instanceof PlayerEntity player)) return;
        var color = DyeColor.byName(NidweenComponents.GLOWING_COLOR.get(player).getColor(), DyeColor.WHITE).getColorComponents();
        outlineVertexConsumerProvider.setColor((int) (color[0] * 255), (int) (color[1] * 255), (int) (color[2] * 255), 255);
    }

}
