package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin<E extends BlockEntity> {

    @Shadow
    private static void runReported(BlockEntity blockEntity, Runnable runnable) {}

    @Shadow
    private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {}

    // I majorly dislike this way of doing it, but without pulling
    // in MixinExtras (which seems a bit excessive for a single inject) I don't
    // think there's much else that can be done
    //
    // glisco, 19.09.2023
    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;isInRenderDistance(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/util/math/Vec3d;)Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void forceRendering(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci, BlockEntityRenderer<E> blockEntityRenderer) {
        if (!MixinHooks.forceBlockEntityRendering) return;

        runReported(blockEntity, () -> render(blockEntityRenderer, blockEntity, tickDelta, matrices, vertexConsumers));
        ci.cancel();
    }

}
