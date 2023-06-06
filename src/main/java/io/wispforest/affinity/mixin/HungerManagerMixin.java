package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @ModifyVariable(method = "update", at = @At(value = "LOAD", ordinal = 0))
    public boolean noHealingForYou(boolean canHeal, PlayerEntity player) {
        var component = AffinityComponents.CHUNK_AETHUM.get(player.world.getChunk(player.getBlockPos()));
        if (!component.isEffectActive(ChunkAethumComponent.NO_NATURAL_REGEN)) return canHeal;

        return false;
    }

}
