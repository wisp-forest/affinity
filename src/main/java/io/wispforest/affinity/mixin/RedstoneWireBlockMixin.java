package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @ModifyReturnValue(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At("TAIL"))
    private static boolean doesNotConnectToRanthracite(boolean original, BlockState state) {
        return !state.isOf(AffinityBlocks.RANTHRACITE_WIRE) && original;
    }

}
