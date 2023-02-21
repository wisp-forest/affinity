package io.wispforest.affinity.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import io.wispforest.affinity.item.AstrokinesisStaffItem;
import io.wispforest.affinity.misc.AstrokinesisStar;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private @Nullable ClientWorld world;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix);

    @Unique
    private final List<AstrokinesisStar> affinity$stars = Stream.generate(AstrokinesisStar::new).limit(100).toList();
    @Unique
    private final Random affinity$random = new Random();

    @Unique
    private float affinity$starAlpha = 0f;

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", ordinal = 1))
    private void renderStuffInTheSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if (!this.world.getDimensionEntry().isIn(AstrokinesisStaffItem.WHITELISTED_DIMENSIONS)) return;

        var player = this.client.player;
        var delta = this.client.getLastFrameDuration() * .05f;
        this.affinity$random.setSeed(6969);

        var potentiallyFrozenStars = new ArrayList<AstrokinesisStar>();
        for (var star : this.affinity$stars) {
            if (star.canBeFrozen()) potentiallyFrozenStars.add(star);
        }

        boolean canFreezeStars = potentiallyFrozenStars.size() >= 10;
        if (canFreezeStars) {
            potentiallyFrozenStars.forEach(star -> star.frozen = true);

            if (!AffinityEntityAddon.hasData(player, AstrokinesisStaffItem.ASTEROID_ORIGIN)) {
                AffinityEntityAddon.setData(player, AstrokinesisStaffItem.ASTEROID_ORIGIN, player.prevYaw);
            }

        } else {
            AffinityEntityAddon.removeData(player, AstrokinesisStaffItem.ASTEROID_ORIGIN);
        }

        boolean performingAstrokinesis = player.getActiveItem().has(AstrokinesisStaffItem.PERFORMING_ASTROKINESIS);
        if (!canFreezeStars || !performingAstrokinesis) {
            for (var star : this.affinity$stars) {
                if (performingAstrokinesis) {
                    float yaw = player.prevYaw % 360f;
                    if (yaw < 0) yaw = 360 + yaw;

                    star.update(new Vector2f(Math.abs((90 + player.prevPitch) % 180), yaw), delta);
                } else {
                    star.frozen = false;
                    star.update(null, delta);
                }
            }
        }

        boolean holdingAstrokinesisStaff = player.isHolding(AffinityItems.ASTROKINESIS_STAFF)
                && player.getItemCooldownManager().getCooldownProgress(AffinityItems.ASTROKINESIS_STAFF, 0) == 0f;

        this.affinity$starAlpha += Delta.compute(this.affinity$starAlpha, holdingAstrokinesisStaff ? 1 : 0, delta * 10);
        if (this.affinity$starAlpha < 0.1) return;

        float baseAlpha = RenderSystem.getShaderColor()[3];
        RenderSystem.getShaderColor()[3] = this.affinity$starAlpha;

        var buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.world.getSkyAngle(tickDelta) * -360f));
        for (var star : affinity$stars) {
            float polar = (float) Math.toRadians(star.polar());
            float azimuthal = (float) Math.toRadians(star.azimuthal());

            float x = (float) (Math.sin(polar) * Math.cos(azimuthal));
            float y = (float) Math.cos(polar);
            float z = (float) (Math.sin(polar) * Math.sin(azimuthal));

            var point = new Vector3f(x, y, z).normalize().mul(100);
            var other = new Vector3f(point).add(this.affinity$random.nextInt(100), this.affinity$random.nextInt(100), this.affinity$random.nextInt(100));

            var upOffset = new Vector3f(point).cross(other).normalize().mul(star.size());
            var rightOffset = new Vector3f(upOffset).rotateAxis((float) (Math.PI / 2), x, y, z).normalize().mul(star.size());

            float alpha = baseAlpha + (1 - baseAlpha) * star.alpha();

            buffer.vertex(matrices.peek().getPositionMatrix(), point.x + rightOffset.x, point.y + rightOffset.y, point.z + rightOffset.z)
                    .color(1, 1, 1, alpha)
                    .next();
            buffer.vertex(matrices.peek().getPositionMatrix(), point.x + upOffset.x + rightOffset.x, point.y + upOffset.y + rightOffset.y, point.z + upOffset.z + rightOffset.z)
                    .color(1, 1, 1, alpha)
                    .next();
            buffer.vertex(matrices.peek().getPositionMatrix(), point.x + upOffset.x, point.y + upOffset.y, point.z + upOffset.z)
                    .color(1, 1, 1, alpha)
                    .next();
            buffer.vertex(matrices.peek().getPositionMatrix(), point.x, point.y, point.z)
                    .color(1, 1, 1, alpha)
                    .next();
        }

        matrices.pop();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        VertexBuffer.unbind();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initSkyBuffer(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        SkyCaptureBuffer.init();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", shift = At.Shift.AFTER))
    private void captureSky(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        SkyCaptureBuffer.captureSky();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void drawSkyAfter(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        SkyCaptureBuffer.draw();
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/DimensionEffects;isDarkened()Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderSkyStencilLayer(MatrixStack matrices, float tickDelta, long arg2, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci, Profiler profiler, boolean bl, Vec3d vec3d, double d, double e, double f, Matrix4f matrix4f, boolean bl2, Frustum frustum, float g, boolean bl3) {
        this.renderLayer(SkyCaptureBuffer.SKY_STENCIL_LAYER, matrices, d, e, f, positionMatrix);
    }

}
