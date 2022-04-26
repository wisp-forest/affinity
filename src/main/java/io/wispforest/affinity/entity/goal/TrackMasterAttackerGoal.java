package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.enchantment.impl.GravecallerEnchantment;
import io.wispforest.affinity.misc.AffinityEntityAddon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;

import java.util.EnumSet;

public class TrackMasterAttackerGoal extends TrackTargetGoal {

    private LivingEntity attacker;
    private int lastAttackedTime;

    public TrackMasterAttackerGoal(MobEntity entity) {
        super(entity, false);
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (AffinityEntityAddon.hasData(mob, GravecallerEnchantment.MASTER_KEY)) {
            final var master = AffinityEntityAddon.getData(mob, GravecallerEnchantment.MASTER_KEY);

            this.attacker = master.getAttacker();
            return master.getLastAttackedTime() != this.lastAttackedTime && this.canTrack(this.attacker, TargetPredicate.DEFAULT);
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        final var master = AffinityEntityAddon.getData(mob, GravecallerEnchantment.MASTER_KEY);
        if (master != null) {
            this.lastAttackedTime = master.getLastAttackedTime();
        }

        super.start();
    }
}
