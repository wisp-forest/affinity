package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.VoidBeaconBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class VoidBeaconBlockEntityRenderer implements BlockEntityRenderer<VoidBeaconBlockEntity> {

    public VoidBeaconBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(VoidBeaconBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.active()) return;

        matrices.push();
        matrices.scale(1, -1, 1);
        BeaconBlockEntityRenderer.renderBeam(
                matrices,
                vertexConsumers,
                Affinity.id("textures/entity/void_beacon_beam.png"),
                tickDelta,
                1f,
                entity.getWorld().getTime(),
                -1,
                300,
                new float[]{1f, 1f, 1f},
                .15f,
                .175f
        );
        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(VoidBeaconBlockEntity beaconBlockEntity) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public boolean isInRenderDistance(VoidBeaconBlockEntity beaconBlockEntity, Vec3d vec3d) {
        return Vec3d.ofCenter(beaconBlockEntity.getPos()).multiply(1, 0, 1).isInRange(vec3d.multiply(1, 0, 1), this.getRenderDistance());
    }
}
