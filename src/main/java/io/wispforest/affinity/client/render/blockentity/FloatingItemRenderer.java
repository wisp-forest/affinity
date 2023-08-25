package io.wispforest.affinity.client.render.blockentity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FloatingItemRenderer {

    public float x = 0f, y = 0f, z = 0f;
    public float scale = 1f;
    public float animationSpeed = 1;

    public float zRotation = 0f;
    public float bobScale = 0f;

    private final ItemRenderer renderer;

    public FloatingItemRenderer(ItemRenderer renderer) {
        this.renderer = renderer;
    }

    protected void renderFloatingItem(MatrixStack matrices, VertexConsumerProvider consumers, float time, long seed, ItemStack stack, @Nullable World world, int light, int overlay) {
        if (stack.isEmpty()) return;

        matrices.push();

        double renderTime = time / 5000 * animationSpeed + seed % 25000;
        float renderScale = this.scale;

        var depthModel = this.renderer.getModel(stack, world, null, 0).hasDepth();
        if (depthModel) renderScale = renderScale * 1.2f;

        if (this.bobScale != 0f) {
            matrices.translate(0, Math.sin(renderTime) * this.bobScale, 0);
        }

        matrices.translate(this.x, depthModel ? this.y - .05 : this.y, this.z);
        matrices.scale(renderScale, renderScale, renderScale);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) ((renderTime) % (2 * Math.PI))));

        if (this.zRotation != 0f) {
            matrices.translate(0, .125, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.zRotation));
            matrices.translate(0, -.125, 0);
        }

        this.renderer.renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, consumers, world, 0);

        matrices.pop();
    }

}
