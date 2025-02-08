package io.wispforest.affinity.mixin.access;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExperienceOrbEntity.class)
public interface ExperienceOrbEntityAccessor {

    @Accessor("amount")
    void affinity$setAmount(int amount);

    @Accessor("pickingCount")
    int affinity$getPickingCount();

    @Accessor("pickingCount")
    void affinity$setPickingCount(int pickingCount);

    @Invoker("repairPlayerGears")
    int affinity$repairPlayerGears(PlayerEntity player, int amount);
}
