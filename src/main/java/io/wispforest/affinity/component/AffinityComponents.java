package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import io.wispforest.affinity.Affinity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

public class AffinityComponents implements EntityComponentInitializer, ChunkComponentInitializer, WorldComponentInitializer, ScoreboardComponentInitializer {

    public static final ComponentKey<GlowingColorComponent> GLOWING_COLOR =
            ComponentRegistry.getOrCreate(Affinity.id("glowing_color"), GlowingColorComponent.class);
    public static final ComponentKey<PlayerAethumComponent> PLAYER_AETHUM =
            ComponentRegistry.getOrCreate(Affinity.id("player_aethum"), PlayerAethumComponent.class);
    public static final ComponentKey<ChunkAethumComponent> CHUNK_AETHUM =
            ComponentRegistry.getOrCreate(Affinity.id("chunk_aethum"), ChunkAethumComponent.class);
    public static final ComponentKey<EntityFlagComponent> ENTITY_FLAGS =
            ComponentRegistry.getOrCreate(Affinity.id("entity_flags"), EntityFlagComponent.class);
    public static final ComponentKey<BanishmentComponent> BANISHMENT =
            ComponentRegistry.getOrCreate(Affinity.id("banishment"), BanishmentComponent.class);
    public static final ComponentKey<WorldPinsComponent> WORLD_PINS =
            ComponentRegistry.getOrCreate(Affinity.id("world_pins"), WorldPinsComponent.class);
    public static final ComponentKey<LocalWeatherComponent> LOCAL_WEATHER =
            ComponentRegistry.getOrCreate(Affinity.id("local_weather"), LocalWeatherComponent.class);
    public static final ComponentKey<PlayerWeatherTrackerComponent> PLAYER_WEATHER_TRACKER =
            ComponentRegistry.getOrCreate(Affinity.id("player_weather_tracker"), PlayerWeatherTrackerComponent.class);
    public static final ComponentKey<ExtraArrowDamageComponent> EXTRA_ARROW_DAMAGE =
            ComponentRegistry.getOrCreate(Affinity.id("extra_arrow_damage"), ExtraArrowDamageComponent.class);
    public static final ComponentKey<InnerCreeperComponent> INNER_CREEPER =
            ComponentRegistry.getOrCreate(Affinity.id("inner_creeper"), InnerCreeperComponent.class);
    public static final ComponentKey<EtherealNodeStorageComponent> ETHEREAL_NODE_STORAGE =
            ComponentRegistry.getOrCreate(Affinity.id("ethereal_node_storage"), EtherealNodeStorageComponent.class);
    public static final ComponentKey<EvadeComponent> EVADE =
            ComponentRegistry.getOrCreate(Affinity.id("evade"), EvadeComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_AETHUM, PlayerAethumComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PLAYER_WEATHER_TRACKER, PlayerWeatherTrackerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(EVADE, EvadeComponent::new, RespawnCopyStrategy.NEVER_COPY);

        registry.registerFor(Entity.class, GLOWING_COLOR, GlowingColorComponent::new);
        registry.registerFor(Entity.class, ENTITY_FLAGS, entity -> new EntityFlagComponent());
        registry.registerFor(LivingEntity.class, BANISHMENT, player -> new BanishmentComponent());
        registry.registerFor(LivingEntity.class, INNER_CREEPER, InnerCreeperComponent::new);
        registry.registerFor(PersistentProjectileEntity.class, EXTRA_ARROW_DAMAGE, player -> new ExtraArrowDamageComponent());
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(CHUNK_AETHUM, ChunkAethumComponent::new);
        registry.register(LOCAL_WEATHER, LocalWeatherComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(WORLD_PINS, WorldPinsComponent::new);
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(ETHEREAL_NODE_STORAGE, (scoreboard, server) -> new EtherealNodeStorageComponent());
    }
}
