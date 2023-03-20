package io.wispforest.affinity.entity;

import io.wispforest.affinity.entity.goal.WispMoveTowardsRitualCoreGoal;
import io.wispforest.affinity.entity.goal.WispTemptGoal;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.wisps.AffinityWispTypes;
import io.wispforest.affinity.object.wisps.WispType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;

public class WiseWispEntity extends WispEntity {

    public WiseWispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new WispTemptGoal(this, .6f, Ingredient.ofItems(AffinityItems.AZALEA_FLOWERS), false));
        this.goalSelector.add(5, new WispMoveTowardsRitualCoreGoal(this));
    }

    @Override
    protected void tickServer() {

    }

    @Override
    protected void tickClient() {

    }

    @Override
    public WispType type() {
        return AffinityWispTypes.WISE;
    }
}
