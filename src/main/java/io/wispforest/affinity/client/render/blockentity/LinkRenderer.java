package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class LinkRenderer {

    public static final Identifier LINK_TEXTURE_ID = Affinity.id("textures/aethum_link.png");
    private static final Matrix4f BILLBOARD_MATRIX = new Matrix4f();

    private static final List<Link> LINKS = new ArrayList<>();

    public static void addLink(Vec3d from, Vec3d to, int color) {
        LINKS.add(new Link(from, to, color));
    }

    public static void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        var linkBuffer = vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(LINK_TEXTURE_ID, true));

        for (var link : LINKS) {
            matrices.push();
            matrices.multiplyPositionMatrix(BILLBOARD_MATRIX.billboardCylindrical(
                    link.from.toVector3f(),
                    cameraPos,
                    link.to.subtract(link.from).toVector3f()
            ));

            linkBuffer.vertex(matrices.peek().getPositionMatrix(), .035f, 0, 0)
                    .color(link.color).texture(0, 1).light(light)
                    .normal(matrices.peek(), 0, 1, 0);

            linkBuffer.vertex(matrices.peek().getPositionMatrix(), .035f, 1, 0)
                    .color(link.color).texture(1, 1).light(light)
                    .normal(matrices.peek(), 0, 1, 0);

            linkBuffer.vertex(matrices.peek().getPositionMatrix(), -.035f, 1, 0)
                    .color(link.color).texture(1, 0).light(light)
                    .normal(matrices.peek(), 0, 1, 0);

            linkBuffer.vertex(matrices.peek().getPositionMatrix(), -.035f, 0, 0)
                    .color(link.color).texture(0, 0).light(light)
                    .normal(matrices.peek(), 0, 1, 0);

            matrices.pop();
        }
    }

    public static void reset() {
        LINKS.clear();
    }

    static {
        WorldRenderEvents.START.register(context -> {
            reset();
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            var matrices = context.matrixStack();
            var camera = context.camera().getPos();

            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            draw(matrices, context.consumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
            matrices.pop();

            reset();
            ((VertexConsumerProvider.Immediate) context.consumers()).draw();
        });
    }

    public record Link(Vec3d from, Vec3d to, int color) {}
}
