package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.misc.AffinityEntityAddon;
import io.wispforest.affinity.misc.LivingEntityTickEvent;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

public class GravecallerEnchantment extends AbsoluteEnchantment {

    public static final AffinityEntityAddon.DataKey<SpawnerLogic> SPAWNER_KEY = AffinityEntityAddon.DataKey.withDefaultFactory(SpawnerLogic::new);
    public static final AffinityEntityAddon.DataKey<LivingEntity> MASTER_KEY = AffinityEntityAddon.DataKey.withNullDefault();

    public GravecallerEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, Type.ARMOR, 205);
    }

    public void serverTick(LivingEntity bearer) {
        final var undeadEntities = bearer.world.getOtherEntities(
                null,
                bearer.getBoundingBox().expand(20),
                entity -> entity instanceof LivingEntity living && living.getGroup() == EntityGroup.UNDEAD);

        for (var undead : undeadEntities) AffinityEntityAddon.setData(undead, MASTER_KEY, bearer);

        var spawner = AffinityEntityAddon.getDataOrSetDefault(bearer, SPAWNER_KEY);
        spawner.serverTick((ServerWorld) bearer.world, bearer.getBlockPos());
    }

    public void clientTick(LivingEntity bearer) {

    }

    static {
        LivingEntityTickEvent.EVENT.register(entity -> {
            if (!AffinityEnchantments.GRAVECALLER.hasCompleteArmor(entity)) return;

            if (!entity.world.isClient) {
                AffinityEnchantments.GRAVECALLER.serverTick(entity);
            } else {
                AffinityEnchantments.GRAVECALLER.clientTick(entity);
            }
        });
    }

    public static class SpawnerLogic extends MobSpawnerLogic {

        private SpawnerLogic() {
            this.setEntityId(EntityType.ZOMBIE);
        }

        @Override
        public void sendStatus(World world, BlockPos pos, int i) {}
    }
}
