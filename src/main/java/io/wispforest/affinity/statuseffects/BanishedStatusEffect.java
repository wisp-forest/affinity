package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.item.EchoShardExtension;
import io.wispforest.affinity.misc.EntityTeleporter;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BanishedStatusEffect extends AffinityStatusEffect {

    public BanishedStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
                if (PotionUtil.getPotionEffects(stack).stream().noneMatch(x -> x.getEffectType() == AffinityStatusEffects.BANISHED)) {
                    return;
                }
                if (!stack.has(PotionMixture.EXTRA_DATA)) return;

                EchoShardExtension.formatLocationTooltip(stack.get(PotionMixture.EXTRA_DATA), lines);
            });
        }
    }

    @Override
    public void onRemoved(final LivingEntity outerEntity, AttributeContainer attributes, int amplifier) {
        if (outerEntity.getWorld().isClient) return;

        ServerTasks.doNext(server -> {
            var entity = outerEntity;

            var component = entity.getComponent(AffinityComponents.BANISHMENT);
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
    public void onPotionApplied(LivingEntity target, @Nullable NbtCompound extraData) {
        if (extraData == null) return;
        if (target.getWorld().isClient) return;

        if (target.hasStatusEffect(AffinityStatusEffects.BANISHED)) {
            target.removeStatusEffectInternal(AffinityStatusEffects.BANISHED);
        }

        var component = target.getComponent(AffinityComponents.BANISHMENT);
        component.pos = target.getBlockPos();
        component.dimension = target.getWorld().getRegistryKey().getValue();

        var pos = extraData.get(EchoShardExtension.POS);
        var targetWorldId = extraData.get(EchoShardExtension.WORLD);
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
