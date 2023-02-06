package io.wispforest.affinity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.client.render.CrosshairStatProvider;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.util.Drawer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class StatProviderRenderer {

    private static BlockPos lastTargetPos = null;
    private static float targetViewTime = -1;

    static void initialize() {
        WorldRenderEvents.LAST.register(context -> {
            var client = MinecraftClient.getInstance();

            if (client.crosshairTarget instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {

                var target = blockHit.getBlockPos();
                if (!target.equals(lastTargetPos)) {
                    lastTargetPos = target;
                    targetViewTime = 0;
                }

                targetViewTime += client.getLastFrameDuration();
                if (targetViewTime < 5) return;

                final var blockEntity = client.world.getBlockEntity(target);
                if (!(blockEntity instanceof CrosshairStatProvider provider)) return;

                var entries = new ArrayList<CrosshairStatProvider.Entry>();
                provider.appendTooltipEntries(entries);

                var modelViewStack = RenderSystem.getModelViewStack();
                modelViewStack.push();

                var pos = Vec3d.ofCenter(target).subtract(context.camera().getPos());
                modelViewStack.translate(pos.x, pos.y + .5f, pos.z);
                modelViewStack.scale(.01f, -.01f, .01f);

                var offset = pos.multiply(-1);
                double horizontalAngle = Math.atan2(offset.z, offset.x);
                double verticalAngle = Math.atan2(offset.y, Math.sqrt(offset.x * offset.x + offset.z * offset.z));

                modelViewStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) (-horizontalAngle + Math.PI / 2)));
                RenderSystem.applyModelViewMatrix();

                var matrices = new MatrixStack();
                matrices.translate(50, 0, 60);
                matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (verticalAngle)));

                for (int i = 0; i < entries.size(); i++) {
                    var entry = entries.get(i);
                    float progress = MathHelper.clamp((targetViewTime - 5 - i * 2) / 15, 0, 1);

                    matrices.push();
                    matrices.translate(0, 0, Easing.CUBIC.apply(1 - progress) * -25);

                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, progress);
                    RenderSystem.setShaderTexture(0, entry.texture());
                    RenderSystem.enableDepthTest();
                    Drawer.drawTexture(matrices, 0, i * 10, entry.x(), entry.y(), 8, 8, 32, 32);

                    client.textRenderer.draw(matrices, entry.text(), 15, i * 10, (Math.max(4, (int) (0xFF * progress)) << 24) | 0xFFFFFF);
                    matrices.pop();
                }

                modelViewStack.pop();
                RenderSystem.applyModelViewMatrix();
            } else {
                lastTargetPos = null;
            }
        });


    }

}
