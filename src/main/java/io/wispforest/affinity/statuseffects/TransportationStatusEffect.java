package io.wispforest.affinity.statuseffects;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.TransportationComponent;
import io.wispforest.affinity.item.EchoShardItem;
import io.wispforest.affinity.misc.EntityTeleporter;
import io.wispforest.affinity.misc.ServerTaskScheduler;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
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

    public static void createCloudFor(LivingEntity entity) {
        AffinityParticleSystems.TRANSPORTATION_CLOUD.spawn(entity.world, entity.getPos());
    }

    @Override
    public void onRemoved(final LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity.world.isClient) return;

        ServerTaskScheduler.scheduleTask(server -> {
            var e = entity;

            TransportationComponent component = AffinityComponents.TRANSPORTATION.get(e);
            ServerWorld w = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, component.getWorld()));
            Vec3d pos = component.getPos();
            createCloudFor(e);
            e = EntityTeleporter.teleport(e, w, pos, e.getYaw(), e.getPitch());
            createCloudFor(e);
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));
        });
    }

    @Override
    public void onPotionApplied(LivingEntity target, @Nullable NbtCompound extraData) {
        if (extraData == null) return;
        if (target.world.isClient) return;

        if (target.hasStatusEffect(AffinityStatusEffects.TRANSPORTATION))
            target.removeStatusEffectInternal(AffinityStatusEffects.TRANSPORTATION);

        var component = AffinityComponents.TRANSPORTATION.get(target);
        component.setPos(target.getPos());
        component.setWorld(target.getWorld().getRegistryKey().getValue());

        var pos = EchoShardItem.POS.get(extraData);
        var targetWorldId = EchoShardItem.WORLD.get(extraData);
        var targetWorld = target.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, targetWorldId));

        ServerTaskScheduler.scheduleTask(server -> {
            createCloudFor(target);
            var newEntity = EntityTeleporter.teleport(target, targetWorld, pos, target.getYaw(), target.getPitch());
            createCloudFor(newEntity);
            newEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));
        });
    }
}
