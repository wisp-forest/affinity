package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.item.EchoShardItem;
import io.wispforest.affinity.misc.EntityTeleporter;
import io.wispforest.affinity.misc.ServerScheduler;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public class TransportationStatusEffect extends AffinityStatusEffect {

    public TransportationStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    static {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!PotionUtil.getPotionEffects(stack).stream().anyMatch(x -> x.getEffectType() == AffinityStatusEffects.TRANSPORTATION)) return;
            if (!stack.hasNbt() || !stack.getNbt().contains("ExtraPotionNbt", NbtElement.COMPOUND_TYPE)) return;

            EchoShardItem.formatLocationTooltip(stack.getSubNbt("ExtraPotionNbt"), lines);
        });
    }

    @Override
    public void onRemoved(final LivingEntity outerEntity, AttributeContainer attributes, int amplifier) {
        if (outerEntity.world.isClient) return;

        ServerScheduler.runInstantly(server -> {
            var entity = outerEntity;

            var component = AffinityComponents.TRANSPORTATION.get(entity);
            var world = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, component.dimension));

            createCloudFor(entity);
            entity = EntityTeleporter.teleport(entity, world, component.pos, entity.getYaw(), entity.getPitch());
            createCloudFor(entity);

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));
        });
    }

    private static void createCloudFor(LivingEntity entity) {
        AffinityParticleSystems.TRANSPORTATION_CLOUD.spawn(entity.world, entity.getPos());
    }

    @Override
    public void onPotionApplied(LivingEntity target, @Nullable NbtCompound extraData) {
        if (extraData == null) return;
        if (target.world.isClient) return;

        if (target.hasStatusEffect(AffinityStatusEffects.TRANSPORTATION))
            target.removeStatusEffectInternal(AffinityStatusEffects.TRANSPORTATION);

        var component = AffinityComponents.TRANSPORTATION.get(target);
        component.pos = target.getPos();
        component.dimension = target.getWorld().getRegistryKey().getValue();

        var pos = EchoShardItem.POS.get(extraData);
        var targetWorldId = EchoShardItem.WORLD.get(extraData);
        var targetWorld = target.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, targetWorldId));

        ServerScheduler.runInstantly(server -> {
            createCloudFor(target);
            var newEntity = EntityTeleporter.teleport(target, targetWorld, pos, target.getYaw(), target.getPitch());
            createCloudFor(newEntity);
            newEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));
        });
    }
}
