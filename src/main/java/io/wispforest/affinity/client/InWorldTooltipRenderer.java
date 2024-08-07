package io.wispforest.affinity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.owo.ui.core.Easing;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class InWorldTooltipRenderer {

    private static BlockPos lastTargetPos = null;
    private static float targetViewTime = -1;

    static void initialize() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            var client = MinecraftClient.getInstance();

            if (client.crosshairTarget instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {

                var target = blockHit.getBlockPos();
                if (!target.equals(lastTargetPos)) {
                    lastTargetPos = target;
                    targetViewTime = 0;
                }

                targetViewTime += client.getRenderTickCounter().getLastFrameDuration();
                if (targetViewTime < 5) return;

                final var blockEntity = client.world.getBlockEntity(target);
                if (!(blockEntity instanceof InWorldTooltipProvider provider)) return;

                provider.updateTooltipEntries(Math.floor(targetViewTime) == 5f, client.getRenderTickCounter().getLastFrameDuration());

                var entries = new ArrayList<InWorldTooltipProvider.Entry>();
                provider.appendTooltipEntries(entries);

                var modelViewStack = RenderSystem.getModelViewStack();
                modelViewStack.pushMatrix();
                modelViewStack.mul(context.matrixStack().peek().getPositionMatrix());

                var targetShape = blockEntity.getCachedState().getOutlineShape(client.world, target).getBoundingBox();
                var pos = provider.applyTooltipOffset(Vec3d.of(target)
                        .subtract(context.camera().getPos())
                        .add(targetShape.minX + (targetShape.maxX - targetShape.minX) / 2, targetShape.maxY + .15, targetShape.minZ + (targetShape.maxZ - targetShape.minZ) / 2));

                modelViewStack.translate((float) pos.x, (float) pos.y, (float) pos.z);
                modelViewStack.scale(.01f, -.01f, .01f);

                var offset = pos.multiply(-1);
                double horizontalAngle = Math.atan2(offset.z, offset.x);
                double verticalAngle = Math.atan2(offset.y, Math.sqrt(offset.x * offset.x + offset.z * offset.z));

                modelViewStack.rotate(RotationAxis.POSITIVE_Y.rotation((float) (-horizontalAngle + Math.PI / 2)));
                RenderSystem.applyModelViewMatrix();

                var drawContext = new DrawContext(MinecraftClient.getInstance(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
                var matrices = drawContext.getMatrices();
                matrices.translate(50, 0, 60);
                matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (verticalAngle)));

                for (int i = 0; i < entries.size(); i++) {
                    var entry = entries.get(i);
                    float progress = MathHelper.clamp((targetViewTime - 5 - i * 2) / 15, 0, 1);

                    matrices.push();
                    matrices.translate(0, 0, Easing.CUBIC.apply(1 - progress) * -25);

                    if (entry instanceof InWorldTooltipProvider.TextEntry textEntry) {
                        drawContext.drawText(client.textRenderer, textEntry.icon(), 1, i * 10, (Math.max(4, (int) (0xFF * progress)) << 24) | 0xFFFFFF, false);
                    } else if (entry instanceof InWorldTooltipProvider.TextAndIconEntry iconEntry) {
                        RenderSystem.enableBlend();
                        RenderSystem.setShaderColor(1, 1, 1, progress);
                        RenderSystem.setShaderTexture(0, iconEntry.texture());
                        RenderSystem.enableDepthTest();
                        drawContext.drawTexture(iconEntry.texture(), 0, i * 10, iconEntry.u(), iconEntry.v(), 8, 8, 32, 32);
                    }

                    drawContext.drawText(client.textRenderer, entry.label(), 15, i * 10, (Math.max(4, (int) (0xFF * progress)) << 24) | 0xFFFFFF, false);
                    matrices.pop();
                }

                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

                modelViewStack.popMatrix();
                RenderSystem.applyModelViewMatrix();
            } else {
                lastTargetPos = null;
            }
        });


    }

}
