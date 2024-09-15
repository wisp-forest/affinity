package io.wispforest.affinity.misc.quack;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;

import java.util.function.Consumer;

public interface AffinityServerPlayerInteractionManagerExtension {
    void affinity$setBlockBreakingListener(Consumer<ServerPlayerEntity> listener);
}
