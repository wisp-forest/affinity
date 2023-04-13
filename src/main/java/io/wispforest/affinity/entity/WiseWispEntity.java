package io.wispforest.affinity.entity;

import io.wispforest.affinity.entity.goal.WispFleeFromPlayerGoal;
import io.wispforest.affinity.entity.goal.WispMoveTowardsRitualCoreGoal;
import io.wispforest.affinity.entity.goal.WispTemptGoal;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.wisps.AffinityWispTypes;
import io.wispforest.affinity.object.wisps.WispType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class WiseWispEntity extends WispEntity {

    private int scaredTicks = 0;

    public WiseWispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new WispFleeFromPlayerGoal(this, () -> this.scaredTicks > 0));
        this.goalSelector.add(1, new WispTemptGoal(this, .6f, Ingredient.ofItems(AffinityItems.AZALEA_FLOWERS), false));
        this.goalSelector.add(2, new WispMoveTowardsRitualCoreGoal(this));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        var damaged = super.damage(source, amount);

        if (damaged && source.getAttacker() instanceof PlayerEntity) {
            for (var wisp : this.world.getEntitiesByClass(WispEntity.class, new Box(this.getBlockPos()).expand(8), wisp -> wisp instanceof WiseWispEntity)) {
                ((WiseWispEntity) wisp).scaredTicks = 600;
            }
        }

        return damaged;
    }

    @Override
    protected void tickServer() {
        this.scaredTicks = Math.max(0, this.scaredTicks - 1);
    }

    @Override
    protected void tickClient() {

    }

    @Override
    public WispType type() {
        return AffinityWispTypes.WISE;
    }
}
