package io.wispforest.affinity.mixin.access;

import net.minecraft.world.World;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("neighborUpdater")
    NeighborUpdater affinity$getNeighborUpdater();
}
