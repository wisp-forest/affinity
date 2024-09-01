package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.item.EchoShardExtension;
import io.wispforest.affinity.misc.EntityTeleporter;
import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BanishedStatusEffect extends AffinityStatusEffect {

    public BanishedStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    static {
        if (Affinity.onClient()) {
            ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
                if (PotionUtil.getPotionEffects(stack).stream().noneMatch(x -> x.getEffectType().value() == AffinityStatusEffects.BANISHED)) {
                    return;
                }

                var location = stack.get(EchoShardExtension.COMPONENT);
                if (location == null) return;

                EchoShardExtension.formatLocationTooltip(location, lines);
            });
        }
    }

    @Override
    public void onRemovedFromEntity(LivingEntity outerEntity) {
        if (outerEntity.getWorld().isClient) return;

        ServerTasks.doNext(server -> {
            var entity = outerEntity;

            var component = entity.getComponent(AffinityComponents.BANISHMENT);
            if (component.dimension == null || component.pos == null) return;

            var world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, component.dimension));

            spawnCloud(entity);
            entity = EntityTeleporter.teleport(entity, world, Vec3d.ofCenter(component.pos).add(0, .25, 0), entity.getYaw(), entity.getPitch());
            spawnCloud(entity);

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));
        });
    }

    private static void spawnCloud(LivingEntity entity) {
        AffinityParticleSystems.BANISHMENT_CLOUD.spawn(entity.getWorld(), entity.getPos());
    }

    @Override
    public void onPotionApplied(LivingEntity target, @Nullable ComponentMap extraData) {
        if (extraData == null) return;
        if (target.getWorld().isClient) return;

        // TODO: migrate AffinityStatusEffects to registry entries
        RegistryEntry<StatusEffect> banishedEntry = Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.BANISHED);
        if (target.hasStatusEffect(banishedEntry)) {
            target.removeStatusEffectInternal(banishedEntry);
        }

        var component = target.getComponent(AffinityComponents.BANISHMENT);
        component.pos = target.getBlockPos();
        component.dimension = target.getWorld().getRegistryKey().getValue();

        var location = extraData.get(EchoShardExtension.COMPONENT);
        var pos = location.pos();
        var targetWorldId = location.world();
        var targetWorld = target.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, targetWorldId));

        if (targetWorld == null) return;

        ServerTasks.doNext(server -> {
            spawnCloud(target);
            var newEntity = EntityTeleporter.teleport(target, targetWorld, Vec3d.ofCenter(pos), target.getYaw(), target.getPitch());
            spawnCloud(newEntity);
            newEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));
        });
    }
}
