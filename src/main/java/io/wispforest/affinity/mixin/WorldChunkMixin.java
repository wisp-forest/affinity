package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockEntityProvider;createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;"))
    private BlockEntity insertQueueBlockEntity(BlockEntityProvider instance, BlockPos blockPos, BlockState state, Operation<BlockEntity> original) {
        if (MixinHooks.queuedBlockEntity != null) {
            var queuedBlockentity = MixinHooks.queuedBlockEntity;
            MixinHooks.queuedBlockEntity = null;

            return queuedBlockentity;
        } else {
            return original.call(instance, blockPos, state);
        }
    }

}
