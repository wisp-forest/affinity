package io.wispforest.affinity.mixin;

import io.wispforest.affinity.entity.goal.InnerCreeperActiveTargetGoal;
import io.wispforest.affinity.entity.goal.InnerCreeperExplodeGoal;
import io.wispforest.affinity.entity.goal.InnerCreeperFleeEntityGoal;
import io.wispforest.affinity.entity.goal.InnerCreeperMeleeAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin extends MobEntity {

    protected PathAwareEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectCreeperGoals(CallbackInfo ci) {
        final var mob = (PathAwareEntity) (Object) this;
        this.goalSelector.add(-3, new InnerCreeperFleeEntityGoal<>(mob, OcelotEntity.class, 6f, 1, 1.2));
        this.goalSelector.add(-3, new InnerCreeperFleeEntityGoal<>(mob, CatEntity.class, 6f, 1, 1.2));
        this.goalSelector.add(-2, new InnerCreeperExplodeGoal(mob));
        this.goalSelector.add(-1, new InnerCreeperMeleeAttackGoal(mob, 1, false));

        this.targetSelector.add(-5, new InnerCreeperActiveTargetGoal<>(mob, PlayerEntity.class, true));
    }

}
