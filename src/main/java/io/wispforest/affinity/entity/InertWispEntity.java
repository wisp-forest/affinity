package io.wispforest.affinity.entity;

import io.wispforest.affinity.init.AffinityWispTypes;
import io.wispforest.affinity.init.WispType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class InertWispEntity extends WispEntity {

    public InertWispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected WispType type() {
        return AffinityWispTypes.INERT;
    }

    @Override
    protected void tickClient() {

    }

    @Override
    protected void tickServer() {

    }
}
