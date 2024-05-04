package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @ModifyExpressionValue(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z"))
    private boolean startRitualCores(boolean original, @Local(argsOnly = true) ServerPlayerEntity player, @Local(argsOnly = true) World world, @Local(ordinal = 0) BlockPos pos) {
        if (!original || !player.getMainHandStack().isEmpty()) return original;

        var state = world.getBlockState(pos);
        return !(state.isOf(AffinityBlocks.ASP_RITE_CORE) || state.isOf(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS));
    }
}
