package io.wispforest.affinity.object;

import io.wispforest.affinity.entity.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

public class AffinityEntities implements AutoRegistryContainer<EntityType<?>> {

    public static final EntityType<InertWispEntity> INERT_WISP = createWispType(InertWispEntity::new);
    public static final EntityType<WiseWispEntity> WISE_WISP = createWispType(WiseWispEntity::new);
    public static final EntityType<ViciousWispEntity> VICIOUS_WISP = createWispType(ViciousWispEntity::new);

    public static final EntityType<AsteroidEntity> ASTEROID = FabricEntityTypeBuilder.create()
            .entityFactory(AsteroidEntity::new)
            .dimensions(EntityDimensions.fixed(.5f, .5f))
            .build();

    private static <W extends WispEntity> EntityType<W> createWispType(EntityType.EntityFactory<W> factory) {
        return FabricEntityTypeBuilder.<WispEntity>createMob()
                .spawnGroup(SpawnGroup.MONSTER)
                .entityFactory(factory)
                .dimensions(EntityDimensions.fixed(.25f, .25f))
                .spawnRestriction(SpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.WORLD_SURFACE, WispEntity::isValidSpawn)
                .defaultAttributes(WispEntity::createWispAttributes).build();
    }

    @Override
    public Registry<EntityType<?>> getRegistry() {
        return Registries.ENTITY_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<EntityType<?>> getTargetFieldType() {
        return (Class<EntityType<?>>) (Object) EntityType.class;
    }
}
