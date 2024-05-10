package io.wispforest.affinity.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void blockArtifactBladeUseWhenNotAttacking(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        var playerStack = player.getStackInHand(hand);
        if (!(playerStack.getItem() instanceof ArtifactBladeItem) || MinecraftClient.getInstance().options.attackKey.isPressed()) return;

        cir.setReturnValue(ActionResult.PASS);
    }

    @ModifyExpressionValue(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"))
    private boolean startRitualCores(boolean original, @Local(argsOnly = true) ClientPlayerEntity player, @Local(ordinal = 0) BlockPos pos) {
        if (!original || !player.getMainHandStack().isEmpty()) return original;

        var state = this.client.world.getBlockState(pos);
        return !(state.isOf(AffinityBlocks.ASP_RITE_CORE) || state.isOf(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS));
    }
}
