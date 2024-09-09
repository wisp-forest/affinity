package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.VillagerArmatureBlock;
import io.wispforest.affinity.blockentity.impl.VillagerArmatureBlockEntity;
import io.wispforest.owo.ui.core.Easing;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VillagerArmatureBlockEntityRenderer extends AffinityBlockEntityRenderer<VillagerArmatureBlockEntity> {

    public static final Transformation.Interpolation EXPO = (result, delta, keyframes, start, end, scale) -> {
        var from = keyframes[start].target();
        var to = keyframes[end].target();

        return from.lerp(to, Easing.CUBIC.apply(delta), result).mul(scale);
    };

    public static final Animation PUNCH_ANIMATION = Animation.Builder.create(0.3f)
        .addBoneAnimation("arms", new Transformation(Transformation.Targets.ROTATE,
            new Keyframe(0f, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), EXPO),
            new Keyframe(0.15f, AnimationHelper.createRotationalVector(-17.5F, 0.0F, 0.0F), EXPO),
            new Keyframe(0.3f, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), EXPO)
        ))
        .addBoneAnimation("arms", new Transformation(Transformation.Targets.TRANSLATE,
            new Keyframe(0f, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), EXPO),
            new Keyframe(0.15f, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 1.5F), EXPO),
            new Keyframe(0.3f, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), EXPO)
        ))
        .build();

    private final ModelPart arms;

    public VillagerArmatureBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);

        var rootPart = new ModelData().getRoot();
        rootPart.addChild(
            "arms",
            ModelPartBuilder.create()
                .uv(0, 16)
                .cuboid(-4.0F, 2.1618F, -2.0F, 8.0F, 4.0F, 4.0F)
                .uv(4, 0)
                .cuboid(-8.0F, -1.8382F, -2.0F, 4.0F, 8.0F, 4.0F)
                .uv(4, 0)
                .mirrored()
                .cuboid(4.0F, -1.8382F, -2.0F, 4.0F, 8.0F, 4.0F)
                .mirrored(false),
            ModelTransform.of(0.0F, 11.6667F, 2.0F, 2.356f, 0.0F, 0.0F)
        );

        this.arms = rootPart.createPart(32, 32);
    }

    @Override
    protected void render(VillagerArmatureBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var animationState = entity.punchAnimationState;

        this.arms.traverse().forEach(ModelPart::resetTransform);
        animationState.update(entity.time() + tickDelta, 1f);
        animationState.run(state -> animate(this.arms, PUNCH_ANIMATION, state.getTimeRunning(), 1f, new Vector3f()));

        matrices.push();
        matrices.translate(.5, 0, .5);
        matrices.multiply(entity.getCachedState().get(VillagerArmatureBlock.FACING).getRotationQuaternion().rotateX((float) (Math.PI / -2)));

        var buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(Affinity.id("textures/item/villager_arms.png")));
        this.arms.render(matrices, buffer, light, overlay);

        if (!entity.heldStack().isEmpty()) {
            this.arms.getChild("arms").rotate(matrices);
            matrices.translate(0, .325, -.1);
            matrices.multiply(new Quaternionf().rotationY((float) Math.PI).rotateX((float) Math.toRadians(100)));
            this.ctx.getItemRenderer().renderItem(entity.heldStack(), ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
        }

        matrices.pop();
    }

    // stolen straight from AnimationHelper
    @SuppressWarnings("SameParameterValue")
    private static void animate(ModelPart model, Animation animation, long runningTime, float scale, Vector3f tempVec) {
        float f = getRunningSeconds(animation, runningTime);

        for (var entry : animation.boneAnimations().entrySet()) {
            if (!model.hasChild(entry.getKey())) continue;

            var part = model.getChild(entry.getKey());
            var list = entry.getValue();

            list.forEach(transformation -> {
                Keyframe[] keyframes = transformation.keyframes();
                int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, index -> f <= keyframes[index].timestamp()) - 1);
                int j = Math.min(keyframes.length - 1, i + 1);
                Keyframe keyframe = keyframes[i];
                Keyframe keyframe2 = keyframes[j];
                float h = f - keyframe.timestamp();
                float k;
                if (j != i) {
                    k = MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0F, 1.0F);
                } else {
                    k = 0.0F;
                }

                keyframe2.interpolation().apply(tempVec, k, keyframes, i, j, scale);
                transformation.target().apply(part, tempVec);
            });
        }
    }

    private static float getRunningSeconds(Animation animation, long runningTime) {
        float f = (float) runningTime / 1000.0F;
        return animation.looping() ? f % animation.lengthInSeconds() : f;
    }
}
