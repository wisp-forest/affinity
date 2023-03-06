package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.misc.EntityReference;
import io.wispforest.affinity.misc.LivingEntityTickCallback;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GravecallerEnchantment extends AbsoluteEnchantment implements EnchantmentEquipEventReceiver {

    public static final AffinityEntityAddon.DataKey<SpawnerLogic> SPAWNER_KEY = AffinityEntityAddon.DataKey.withDefaultFactory(SpawnerLogic::new);
    public static final AffinityEntityAddon.DataKey<Set<EntityReference<Entity>>> MINIONS_KEY = AffinityEntityAddon.DataKey.withDefaultFactory(HashSet::new);
    public static final AffinityEntityAddon.DataKey<EntityReference<LivingEntity>> MASTER_KEY = AffinityEntityAddon.DataKey.withNullDefault();

    public GravecallerEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, Type.ARMOR, 205);
    }

    public void serverTick(LivingEntity bearer) {
        final var undeadEntities = getUndeadEntities(bearer);
        final var minions = AffinityEntityAddon.getDataOrSetDefault(bearer, MINIONS_KEY);

        for (var undead : undeadEntities) {
            if (AffinityEntityAddon.hasData(undead, MASTER_KEY)) continue;

            AffinityEntityAddon.setData(undead, MASTER_KEY, EntityReference.of(bearer));
            minions.add(EntityReference.of(undead));
        }

        if (undeadEntities.size() > 6 && bearer.world.getTime() % 20 == 0) {
            bearer.heal(undeadEntities.size() - 6);
        }

        var spawner = AffinityEntityAddon.getDataOrSetDefault(bearer, SPAWNER_KEY);
        spawner.serverTick((ServerWorld) bearer.world, bearer.getBlockPos());
    }

    public void clientTick(LivingEntity bearer) {

    }

    private List<Entity> getUndeadEntities(LivingEntity master) {
        return master.world.getOtherEntities(
                null,
                master.getBoundingBox().expand(15),
                entity -> entity instanceof LivingEntity living && living.getGroup() == EntityGroup.UNDEAD);
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot)) return;

        if (!hasCompleteArmor(entity) && AffinityEntityAddon.hasData(entity, MINIONS_KEY)) {
            final var minions = AffinityEntityAddon.removeData(entity, MINIONS_KEY);
            for (var minion : minions) {
                if (!minion.present()) continue;
                AffinityEntityAddon.removeData(minion.get(), MASTER_KEY);
            }
        }
    }

    @Override
    public void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {}

    public static boolean isMaster(Entity undead, Entity potentialMaster) {
        return AffinityEntityAddon.hasData(undead, MASTER_KEY) && AffinityEntityAddon.getData(undead, MASTER_KEY).get() == potentialMaster;
    }

    static {
        LivingEntityTickCallback.EVENT.register(entity -> {
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
            var nbt = new NbtCompound();
            nbt.putString("id", Registries.ENTITY_TYPE.getId(EntityType.ZOMBIE).toString());
            this.setSpawnEntry(null, null, new MobSpawnerEntry(nbt, Optional.empty()));
        }

        @Override
        public void sendStatus(World world, BlockPos pos, int i) {}
    }
}
