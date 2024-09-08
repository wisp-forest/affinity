package io.wispforest.affinity.item;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;

public class VillagerArmsItem extends Item {

    public VillagerArmsItem(Settings settings) {
        super(settings);
    }

    static {
        // let's go steal some villager arms
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!player.shouldCancelInteraction() || !player.getStackInHand(hand).isIn(ItemTags.AXES)) return ActionResult.PASS;
            if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

            var flags = AffinityComponents.ENTITY_FLAGS.get(villager);
            if (flags.hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS)) return ActionResult.PASS;

            // ha! gottem
            flags.setFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS);
            ItemScatterer.spawn(world, villager.getX(), villager.getY(), villager.getZ(), AffinityItems.VILLAGER_ARMS.getDefaultStack());

            villager.playSound(SoundEvents.ITEM_AXE_STRIP);
            player.getStackInHand(hand).damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            return ActionResult.SUCCESS;
        });

        // damn, my conscience
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            var stack = player.getStackInHand(hand);
            if (!stack.isOf(AffinityItems.VILLAGER_ARMS)) return ActionResult.PASS;

            if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

            var flags = AffinityComponents.ENTITY_FLAGS.get(villager);
            if (!flags.hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS)) return ActionResult.PASS;

            // there you go, have them back
            flags.unsetFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS);
            ItemOps.decrementPlayerHandItem(player, hand);

            return ActionResult.SUCCESS;
        });
    }
}
