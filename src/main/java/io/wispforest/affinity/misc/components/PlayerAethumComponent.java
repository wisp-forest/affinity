package io.wispforest.affinity.misc.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAethumComponent extends AethumComponent<PlayerEntity> {

    public PlayerAethumComponent(PlayerEntity holder) {
        super(AffinityComponents.PLAYER_AETHUM, holder);
    }

    @Override
    double defaultValue() {
        return 10;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.holder;
    }
}
