package io.wispforest.affinity.mixin;

import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void doNotYeetNodes(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (!(world.getBlockEntity(pos) instanceof AethumFluxNodeBlockEntity node) || !node.hasShard()) return;

        node.onBreakStart(miner);
        cir.setReturnValue(false);
    }

}
