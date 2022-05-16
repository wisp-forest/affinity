package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.wispforest.affinity.Affinity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class AffinityComponents implements EntityComponentInitializer, ChunkComponentInitializer {

    public static final ComponentKey<GlowingColorComponent> GLOWING_COLOR =
            ComponentRegistry.getOrCreate(Affinity.id("glowing_color"), GlowingColorComponent.class);
    public static final ComponentKey<PlayerAethumComponent> PLAYER_AETHUM =
            ComponentRegistry.getOrCreate(Affinity.id("player_aethum"), PlayerAethumComponent.class);
    public static final ComponentKey<ChunkAethumComponent> CHUNK_AETHUM =
            ComponentRegistry.getOrCreate(Affinity.id("chunk_aethum"), ChunkAethumComponent.class);
    public static final ComponentKey<EntityFlagComponent> ENTITY_FLAGS =
            ComponentRegistry.getOrCreate(Affinity.id("entity_flags"), EntityFlagComponent.class);
    public static final ComponentKey<TransportationComponent> TRANSPORTATION =
            ComponentRegistry.getOrCreate(Affinity.id("transportation"), TransportationComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(GLOWING_COLOR, GlowingColorComponent::new, RespawnCopyStrategy.NEVER_COPY);
        registry.registerForPlayers(PLAYER_AETHUM, PlayerAethumComponent::new, RespawnCopyStrategy.ALWAYS_COPY);

        registry.registerFor(Entity.class, ENTITY_FLAGS, entity -> new EntityFlagComponent());
        registry.registerFor(LivingEntity.class, TRANSPORTATION, player -> new TransportationComponent());
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(CHUNK_AETHUM, ChunkAethumComponent::new);
    }
}
