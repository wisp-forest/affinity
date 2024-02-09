package io.wispforest.affinity.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CuboidRenderer {

    private static int WHITE_TEXTURE_ID = -1;
    private static final RenderPhase.TextureBase WHITE_TEXTURE = new RenderPhase.TextureBase(
            () -> {
                if (WHITE_TEXTURE_ID == -1) {
                    var texture = new NativeImageBackedTexture(1, 1, false);
                    texture.getImage().setColor(0, 0, Color.WHITE.argb());
                    texture.upload();

                    WHITE_TEXTURE_ID = texture.getGlId();
                }

                RenderSystem.setShaderTexture(0, WHITE_TEXTURE_ID);
            }, () -> {}
    );

    public static final RenderLayer BOX_LAYER = RenderLayer.of(
            "affinity:cuboid_box",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
            256, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_SOLID_PROGRAM)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .texture(WHITE_TEXTURE)
                    .target(RenderPhase.MAIN_TARGET)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .build(false)
    );

    public static final RenderLayer OUTLINE_LAYER = RenderLayer.of(
            "affinity:cuboid_outline",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
            512, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_SOLID_PROGRAM)
                    .texture(WHITE_TEXTURE)
                    .target(RenderPhase.MAIN_TARGET)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .build(false)
    );

    private static final Map<BlockPos, Cuboid> CUBOIDS = new HashMap<>();
    private static final Set<BlockPos> UPDATED_OWNERS = new HashSet<>();

    public static void add(BlockPos owner, Cuboid cuboid) {
        UPDATED_OWNERS.add(owner);

        if (CUBOIDS.containsKey(owner)) {
            var currentCuboid = CUBOIDS.get(owner);
            if (currentCuboid.matches(cuboid)) {
                currentCuboid.targetCompleteness = 1f;
            } else {
                cuboid.prepare(owner);
                cuboid.completeness = currentCuboid.completeness;
                CUBOIDS.put(owner, cuboid);
            }
        } else {
            cuboid.prepare(owner);
            CUBOIDS.put(owner, cuboid);
        }
    }

    public static void drawCuboid(MatrixStack matrixStack, VertexConsumerProvider consumers, Cuboid cuboid) {
        drawCuboid(matrixStack, consumers, cuboid, 1f);
    }

    public static void drawCuboid(MatrixStack matrixStack, VertexConsumerProvider consumers, Cuboid cuboid, float completeness) {
        cuboid.completeness = completeness;
        cuboid.prepare(BlockPos.ORIGIN);

        renderCuboidEdges(matrixStack, consumers.getBuffer(OUTLINE_LAYER), cuboid);
        renderCuboidFaces(matrixStack, consumers.getBuffer(BOX_LAYER), MinecraftClient.getInstance().gameRenderer.getCamera().getPos(), cuboid);
    }

    static {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.isPaused()) return;

            var owners = new HashSet<>(CUBOIDS.keySet());
            for (var owner : owners) {
                if (UPDATED_OWNERS.contains(owner)) continue;

                var cuboid = CUBOIDS.get(owner);
                cuboid.targetCompleteness = 0f;

                if (cuboid.completeness < .05) {
                    CUBOIDS.remove(owner);
                }
            }

            UPDATED_OWNERS.clear();
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (CUBOIDS.isEmpty()) return;

            final var matrices = context.matrixStack();
            final var outlineBuffer = context.consumers().getBuffer(OUTLINE_LAYER);

            final var cam = context.camera().getPos();
            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            for (var cuboid : CUBOIDS.values()) {
                cuboid.completeness += Delta.compute(cuboid.completeness, cuboid.targetCompleteness, MinecraftClient.getInstance().getLastFrameDuration() * .25f);
                renderCuboidEdges(matrices, outlineBuffer, cuboid);
            }

            final var boxBuffer = context.consumers().getBuffer(BOX_LAYER);
            for (var cuboid : CUBOIDS.values()) {
                renderCuboidFaces(matrices, boxBuffer, cam, cuboid);
            }

            matrices.pop();
            ((VertexConsumerProvider.Immediate) context.consumers()).draw();
        });
    }

    private static void renderCuboidEdges(MatrixStack matrices, VertexConsumer buffer, Cuboid cuboid) {
        var outlineColor = cuboid.outlineColor;
        var outlineThickness = .02f * cuboid.completeness;

        line(matrices, buffer, cuboid.minX, cuboid.minY, cuboid.minZ, cuboid.minX, cuboid.minY, cuboid.maxZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.maxX, cuboid.minY, cuboid.minZ, cuboid.maxX, cuboid.minY, cuboid.maxZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.minX, cuboid.minY, cuboid.minZ, cuboid.maxX, cuboid.minY, cuboid.minZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.minX, cuboid.minY, cuboid.maxZ, cuboid.maxX, cuboid.minY, cuboid.maxZ, outlineColor, outlineThickness);

        line(matrices, buffer, cuboid.minX, cuboid.maxY, cuboid.minZ, cuboid.minX, cuboid.maxY, cuboid.maxZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.maxX, cuboid.maxY, cuboid.minZ, cuboid.maxX, cuboid.maxY, cuboid.maxZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.minX, cuboid.maxY, cuboid.minZ, cuboid.maxX, cuboid.maxY, cuboid.minZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.minX, cuboid.maxY, cuboid.maxZ, cuboid.maxX, cuboid.maxY, cuboid.maxZ, outlineColor, outlineThickness);

        line(matrices, buffer, cuboid.minX, cuboid.minY, cuboid.minZ, cuboid.minX, cuboid.maxY, cuboid.minZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.minX, cuboid.minY, cuboid.maxZ, cuboid.minX, cuboid.maxY, cuboid.maxZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.maxX, cuboid.minY, cuboid.minZ, cuboid.maxX, cuboid.maxY, cuboid.minZ, outlineColor, outlineThickness);
        line(matrices, buffer, cuboid.maxX, cuboid.minY, cuboid.maxZ, cuboid.maxX, cuboid.maxY, cuboid.maxZ, outlineColor, outlineThickness);
    }

    private static void renderCuboidFaces(MatrixStack matrices, VertexConsumer buffer, Vec3d cam, Cuboid cuboid) {
        var boxColor = new Color(cuboid.fillColor.red(), cuboid.fillColor.green(), cuboid.fillColor.blue(), .15f * cuboid.completeness * cuboid.fillColor.alpha());

        if (cam.x >= cuboid.minX && cam.y >= cuboid.minY && cam.z >= cuboid.minZ && cam.x <= cuboid.maxX && cam.y <= cuboid.maxY && cam.z <= cuboid.maxZ) {
            cuboid(matrices, buffer, cuboid.maxX, cuboid.maxY, cuboid.maxZ, cuboid.minX, cuboid.minY, cuboid.minZ, boxColor);
        } else {
            cuboid(matrices, buffer, cuboid.minX, cuboid.minY, cuboid.minZ, cuboid.maxX, cuboid.maxY, cuboid.maxZ, boxColor);
        }
    }

    public static void line(MatrixStack matrices, VertexConsumer buffer, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, Color color, float thickness) {
        cuboid(matrices, buffer, fromX - thickness, fromY - thickness, fromZ - thickness, toX + thickness, toY + thickness, toZ + thickness, color);
    }

    public static void cuboid(MatrixStack matrices, VertexConsumer buffer, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, Color color) {
        var positionMatrix = matrices.peek().getPositionMatrix();
        var normalMatrix = matrices.peek().getNormalMatrix();
        int argb = color.argb();

        fromX -= 5e-4f;
        fromY -= 5e-4f;
        fromZ -= 5e-4f;
        toX += 5e-4f;
        toY += 5e-4f;
        toZ += 5e-4f;

        vertex(buffer, positionMatrix, normalMatrix, fromX, fromY, fromZ, argb, 0, -1, -0);
        vertex(buffer, positionMatrix, normalMatrix, toX, fromY, fromZ, argb, 0, -1, -0);
        vertex(buffer, positionMatrix, normalMatrix, toX, fromY, toZ, argb, 0, -1, -0);
        vertex(buffer, positionMatrix, normalMatrix, fromX, fromY, toZ, argb, 0, -1, -0);

        vertex(buffer, positionMatrix, normalMatrix, fromX, toY, toZ, argb, 0, 1, 0);
        vertex(buffer, positionMatrix, normalMatrix, toX, toY, toZ, argb, 0, 1, 0);
        vertex(buffer, positionMatrix, normalMatrix, toX, toY, fromZ, argb, 0, 1, 0);
        vertex(buffer, positionMatrix, normalMatrix, fromX, toY, fromZ, argb, 0, 1, 0);

        vertex(buffer, positionMatrix, normalMatrix, fromX, fromY, toZ, argb, -1, 0, 0);
        vertex(buffer, positionMatrix, normalMatrix, fromX, toY, toZ, argb, -1, 0, 0);
        vertex(buffer, positionMatrix, normalMatrix, fromX, toY, fromZ, argb, -1, 0, 0);
        vertex(buffer, positionMatrix, normalMatrix, fromX, fromY, fromZ, argb, -1, 0, 0);

        vertex(buffer, positionMatrix, normalMatrix, toX, fromY, fromZ, argb, 1, 0, 0);
        vertex(buffer, positionMatrix, normalMatrix, toX, toY, fromZ, argb, 1, 0, 0);
        vertex(buffer, positionMatrix, normalMatrix, toX, toY, toZ, argb, 1, 0, 0);
        vertex(buffer, positionMatrix, normalMatrix, toX, fromY, toZ, argb, 1, 0, 0);

        vertex(buffer, positionMatrix, normalMatrix, fromX, fromY, fromZ, argb, 0, 0, -1);
        vertex(buffer, positionMatrix, normalMatrix, fromX, toY, fromZ, argb, 0, 0, -1);
        vertex(buffer, positionMatrix, normalMatrix, toX, toY, fromZ, argb, 0, 0, -1);
        vertex(buffer, positionMatrix, normalMatrix, toX, fromY, fromZ, argb, 0, 0, -1);

        vertex(buffer, positionMatrix, normalMatrix, toX, fromY, toZ, argb, 0, 0, 1);
        vertex(buffer, positionMatrix, normalMatrix, toX, toY, toZ, argb, 0, 0, 1);
        vertex(buffer, positionMatrix, normalMatrix, fromX, toY, toZ, argb, 0, 0, 1);
        vertex(buffer, positionMatrix, normalMatrix, fromX, fromY, toZ, argb, 0, 0, 1);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f position, Matrix3f normal, float x, float y, float z, int color, float normalX, float normalY, float normalZ) {
        buffer.vertex(position, x, y, z).color(color).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(normal, normalX, normalY, normalZ).next();
    }

    public static class Cuboid {

        private final BlockPos from, to;
        private final Color outlineColor, fillColor;

        private float completeness = 0f, targetCompleteness = 1f;
        private float minX, minY, minZ, maxX, maxY, maxZ;

        private Cuboid(BlockPos from, BlockPos to, Color outlineColor, Color fillColor) {
            this.from = from;
            this.to = to;
            this.outlineColor = outlineColor;
            this.fillColor = fillColor;
        }

        public static Cuboid of(BlockPos from, BlockPos to) {
            return new Cuboid(from, to, Color.WHITE, Color.WHITE);
        }

        public static Cuboid of(BlockPos from, BlockPos to, Color outlineColor, Color fillColor) {
            return new Cuboid(from, to, outlineColor, fillColor);
        }

        public static Cuboid symmetrical(int x, int y, int z, Color outlineColor, Color fillColor) {
            return new Cuboid(new BlockPos(-x, -y, -z), new BlockPos(x + 1, y + 1, z + 1), outlineColor, fillColor);
        }

        public static Cuboid symmetrical(int x, int y, int z) {
            return symmetrical(x, y, z, Color.WHITE, Color.WHITE);
        }

        private void prepare(BlockPos owner) {
            var minX = owner.getX() + this.from.getX();
            var minY = owner.getY() + this.from.getY();
            var minZ = owner.getZ() + this.from.getZ();

            var maxX = owner.getX() + this.to.getX();
            var maxY = owner.getY() + this.to.getY();
            var maxZ = owner.getZ() + this.to.getZ();

            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.maxZ = Math.max(minZ, maxZ);
        }

        private boolean matches(Cuboid other) {
            return other.from.equals(this.from)
                    && other.to.equals(this.to)
                    && other.fillColor.equals(this.fillColor)
                    && other.outlineColor.equals(this.outlineColor);
        }
    }
}
