package io.wispforest.affinity.registries;

import io.wispforest.affinity.entity.WispEntity;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;

public class AffinityEntities implements AutoRegistryContainer<EntityType<?>> {

    public static final EntityType<WispEntity> WISP = FabricEntityTypeBuilder.<WispEntity>createMob()
            .spawnGroup(SpawnGroup.MONSTER)
            .entityFactory(WispEntity::new)
            .dimensions(EntityDimensions.fixed(.25f, .25f))
            .spawnRestriction(SpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.WORLD_SURFACE, WispEntity::isValidSpawn)
            .defaultAttributes(WispEntity::createWispAttributes).build();

    @Override
    public Registry<EntityType<?>> getRegistry() {
        return Registry.ENTITY_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<EntityType<?>> getTargetFieldType() {
        return (Class<EntityType<?>>) (Object) EntityType.class;
    }
}