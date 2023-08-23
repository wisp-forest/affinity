package io.wispforest.affinity.mixin;

import io.wispforest.affinity.item.DirectInteractionHandler;
import io.wispforest.affinity.item.WandOfInquiryItem;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void dropAzaleaFlowers(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!state.isOf(Blocks.FLOWERING_AZALEA_LEAVES)) return;

        if (!world.isClient) {
            world.setBlockState(pos, AffinityBlocks.BUDDING_AZALEA_LEAVES.getDefaultState()
                    .with(LeavesBlock.PERSISTENT, state.get(LeavesBlock.PERSISTENT))
                    .with(LeavesBlock.DISTANCE, state.get(LeavesBlock.DISTANCE)));
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AffinityItems.AZALEA_FLOWERS));
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }

    @Mixin(AbstractBlock.AbstractBlockState.class)
    public static class AbstractBlockStateMixin {

        @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
        private void cancelInteractionIfAppropriate(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
            final var playerStack = player.getStackInHand(hand);
            if (!(playerStack.getItem() instanceof DirectInteractionHandler handler)) return;

            if (!handler.shouldHandleInteraction(playerStack, world, hit.getBlockPos(), ((BlockState) (Object) this))) return;
            cir.setReturnValue(ActionResult.PASS);
        }

        @Inject(method = "onBlockBreakStart", at = @At("HEAD"), cancellable = true)
        private void notifyWandOfInquiry(World world, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
            final var playerStack = player.getMainHandStack();
            if (playerStack.getItem() != AffinityItems.WAND_OF_INQUIRY) return;

            if (!WandOfInquiryItem.handleAttackBlock(world, pos)) return;
            ci.cancel();
        }

    }

}
