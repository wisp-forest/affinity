package io.wispforest.affinity.client.render.item;

import io.wispforest.affinity.item.StaffItem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class StaffRevolverRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    public static final StaffRevolverRenderer INSTANCE = new StaffRevolverRenderer();
    private StaffRevolverRenderer() {}

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var bundledStaffs = stack.get(StaffItem.BUNDLED_STAFFS);
        var itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        matrices.translate(.5f, .5f, .5f);

        matrices.push();

        var stackWithoutBundle = stack.copy();
        stackWithoutBundle.remove(StaffItem.BUNDLED_STAFFS);

        var using = MinecraftClient.getInstance().player.getActiveItem() == stack;
        if (using) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
            matrices.translate(.25, 0, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45));
        }

        itemRenderer.renderItem(
                stack,
                ModelTransformationMode.NONE,
                false,
                matrices,
                vertexConsumers,
                light,
                overlay,
                itemRenderer.getModel(stackWithoutBundle, null, null, 0)
        );

        if (using) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
            matrices.translate(-.25, 0, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45));
        }

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
        matrices.translate(-.15, 0, 0);

        var initialAngle = Math.toRadians(System.currentTimeMillis() / 40d % 360d);
        var orbitRadius = using ? .25 : .45;

        for (int i = 0; i < bundledStaffs.size(); i++) {
            var angle = Math.toRadians(i * (360d / bundledStaffs.size())) + initialAngle;

            matrices.push();
            matrices.translate(Math.sin(initialAngle + angle) * .075, Math.cos(angle) * orbitRadius, Math.sin(angle) * orbitRadius);
            matrices.scale(.5f, .5f, .5f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (angle + Math.PI / 2)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45));
            itemRenderer.renderItem(
                    bundledStaffs.get(i),
                    ModelTransformationMode.NONE,
                    false,
                    matrices,
                    vertexConsumers,
                    light,
                    overlay,
                    itemRenderer.getModel(bundledStaffs.get(i), null, null, 0)
            );
            matrices.pop();
        }

        matrices.pop();
    }
}
