package io.wispforest.affinity.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin<E extends BlockEntity> {

    @ModifyExpressionValue(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;isInRenderDistance(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/util/math/Vec3d;)Z"))
    private boolean forceRendering(boolean original) {
        if (!MixinHooks.forceBlockEntityRendering) return original;
        return true;
    }
}
