package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.wispforest.affinity.Affinity;

public class AffinityComponents implements EntityComponentInitializer, ChunkComponentInitializer {

    public static final ComponentKey<GlowingColorComponent> GLOWING_COLOR =
            ComponentRegistry.getOrCreate(Affinity.id("glowing_color"), GlowingColorComponent.class);
    public static final ComponentKey<PlayerAethumComponent> PLAYER_AETHUM =
            ComponentRegistry.getOrCreate(Affinity.id("player_aethum"), PlayerAethumComponent.class);
    public static final ComponentKey<ChunkAethumComponent> CHUNK_AETHUM =
            ComponentRegistry.getOrCreate(Affinity.id("chunk_aethum"), ChunkAethumComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(GLOWING_COLOR, GlowingColorComponent::new, RespawnCopyStrategy.NEVER_COPY);
        registry.registerForPlayers(PLAYER_AETHUM, PlayerAethumComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(CHUNK_AETHUM, ChunkAethumComponent::new);
    }
}
