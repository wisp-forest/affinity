package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.enchantment.GravecallerEnchantmentLogic;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
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
        if (AffinityEntityAddon.hasData(mob, GravecallerEnchantmentLogic.MASTER_KEY)) {
            final var masterRef = AffinityEntityAddon.getData(mob, GravecallerEnchantmentLogic.MASTER_KEY);
            if (!masterRef.present()) {
                AffinityEntityAddon.removeData(mob, GravecallerEnchantmentLogic.MASTER_KEY);
                return false;
            }

            final var master = masterRef.get();
            this.attacker = master.getAttacker();
            return master.getLastAttackedTime() != this.lastAttackedTime && this.canTrack(this.attacker, TargetPredicate.DEFAULT);
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);

        final var masterRef = AffinityEntityAddon.getData(mob, GravecallerEnchantmentLogic.MASTER_KEY);
        if (masterRef != null && masterRef.present()) {
            this.lastAttackedTime = masterRef.get().getLastAttackedTime();
        }

        super.start();
    }
}
