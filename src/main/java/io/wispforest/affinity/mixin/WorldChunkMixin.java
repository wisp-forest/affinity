package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "setBlockState", at = @At(value = "STORE", ordinal = 1))
    private BlockEntity insertQueueBlockEntity(BlockEntity blockEntity) {
        if (MixinHooks.queuedBlockEntity != null) {
            var queuedBlockentity = MixinHooks.queuedBlockEntity;
            MixinHooks.queuedBlockEntity = null;

            return queuedBlockentity;
        } else {
            return blockEntity;
        }
    }

}
