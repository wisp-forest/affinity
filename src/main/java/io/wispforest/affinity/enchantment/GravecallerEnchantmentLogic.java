package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.misc.EntityReference;
import io.wispforest.affinity.misc.callback.ItemEquipEvents;
import io.wispforest.affinity.misc.callback.LivingEntityTickCallback;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GravecallerEnchantmentLogic {
    public static final AffinityEntityAddon.DataKey<SpawnerLogic> SPAWNER_KEY = AffinityEntityAddon.DataKey.withDefaultFactory(SpawnerLogic::new);
    public static final AffinityEntityAddon.DataKey<Set<EntityReference<Entity>>> MINIONS_KEY = AffinityEntityAddon.DataKey.withDefaultFactory(HashSet::new);
    public static final AffinityEntityAddon.DataKey<EntityReference<LivingEntity>> MASTER_KEY = AffinityEntityAddon.DataKey.withNullDefault();

    public static void initialize() {
        ItemEquipEvents.UNEQUIP.register((entity, slot, stack) -> {
            var enchantment = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.GRAVECALLER).orElse(null);
            if (enchantment == null || !enchantment.value().slotMatches(slot)) return;

            if (!AbsoluteEnchantmentLogic.hasCompleteArmor(entity, enchantment) && AffinityEntityAddon.hasData(entity, MINIONS_KEY)) {
                final var minions = AffinityEntityAddon.removeData(entity, MINIONS_KEY);
                for (var minion : minions) {
                    if (!minion.present()) continue;
                    AffinityEntityAddon.removeData(minion.get(), MASTER_KEY);
                }
            }
        });

        LivingEntityTickCallback.EVENT.register(entity -> {
            var enchantment = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.GRAVECALLER).orElse(null);
            if (enchantment == null || !AbsoluteEnchantmentLogic.hasCompleteArmor(entity, enchantment)) return;

            if (!entity.getWorld().isClient) {
                serverTick(entity);
            } else {
                clientTick(entity);
            }
        });
    }

    private static void serverTick(LivingEntity bearer) {
        final var undeadEntities = getUndeadEntities(bearer);
        final var minions = AffinityEntityAddon.getDataOrSetDefault(bearer, MINIONS_KEY);

        for (var undead : undeadEntities) {
            if (AffinityEntityAddon.hasData(undead, MASTER_KEY)) continue;

            AffinityEntityAddon.setData(undead, MASTER_KEY, EntityReference.of(bearer));
            minions.add(EntityReference.of(undead));
        }

        if (undeadEntities.size() > 6 && bearer.getWorld().getTime() % 20 == 0) {
            bearer.heal(undeadEntities.size() - 6);
        }

        var spawner = AffinityEntityAddon.getDataOrSetDefault(bearer, SPAWNER_KEY);
        spawner.serverTick((ServerWorld) bearer.getWorld(), bearer.getBlockPos());
    }

    private static void clientTick(LivingEntity bearer) {

    }

    private static List<Entity> getUndeadEntities(LivingEntity master) {
        return master.getWorld().getOtherEntities(
                null,
                master.getBoundingBox().expand(15),
                entity -> entity instanceof LivingEntity living && living.getType().isIn(EntityTypeTags.UNDEAD));
    }

    public static boolean isMaster(Entity undead, Entity potentialMaster) {
        return AffinityEntityAddon.hasData(undead, MASTER_KEY) && AffinityEntityAddon.getData(undead, MASTER_KEY).get() == potentialMaster;
    }

    public static class SpawnerLogic extends MobSpawnerLogic {

        private SpawnerLogic() {
            var nbt = new NbtCompound();
            nbt.putString("id", Registries.ENTITY_TYPE.getId(EntityType.ZOMBIE).toString());
            this.setSpawnEntry(null, null, new MobSpawnerEntry(nbt, Optional.empty(), Optional.empty()));
        }

        @Override
        public void sendStatus(World world, BlockPos pos, int i) {}
    }
}
