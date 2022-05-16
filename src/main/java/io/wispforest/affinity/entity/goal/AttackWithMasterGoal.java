package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.enchantment.impl.GravecallerEnchantment;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;

import java.util.EnumSet;

public class AttackWithMasterGoal extends TrackTargetGoal {

    private LivingEntity attacking;
    private int lastAttackTime;

    public AttackWithMasterGoal(MobEntity entity) {
        super(entity, false);
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (AffinityEntityAddon.hasData(mob, GravecallerEnchantment.MASTER_KEY)) {
            final var masterRef = AffinityEntityAddon.getData(mob, GravecallerEnchantment.MASTER_KEY);
            if (!masterRef.present()) {
                AffinityEntityAddon.removeData(mob, GravecallerEnchantment.MASTER_KEY);
                return false;
            }

            final var master = masterRef.get();
            this.attacking = master.getAttacking();
            return master.getLastAttackTime() != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT);
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacking);
        final var masterRef = AffinityEntityAddon.getData(mob, GravecallerEnchantment.MASTER_KEY);
        if (masterRef != null && masterRef.present()) {
            this.lastAttackTime = masterRef.get().getLastAttackTime();
        }

        super.start();
    }
}
