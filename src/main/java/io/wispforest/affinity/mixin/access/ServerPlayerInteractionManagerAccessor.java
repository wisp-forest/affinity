package io.wispforest.affinity.mixin.access;

import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerInteractionManager.class)
public interface ServerPlayerInteractionManagerAccessor {

    @Accessor("blockBreakingProgress")
    int affinity$blockBreakingProgress();

}
