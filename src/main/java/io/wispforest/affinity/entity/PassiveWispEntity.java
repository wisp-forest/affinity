package io.wispforest.affinity.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class PassiveWispEntity extends WispEntity {

    public PassiveWispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected int particleColor() {
        return 0x548CFF;
    }

    @Override
    protected void tickClient() {

    }

    @Override
    protected void tickServer() {

    }
}
