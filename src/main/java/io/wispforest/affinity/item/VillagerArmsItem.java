package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerData;

import java.util.List;

import static net.minecraft.village.VillagerProfession.*;

public class VillagerArmsItem extends Item {

    public static final ComponentType<VillagerData> VILLAGER_DATA = Affinity.component("villager_data", CodecUtils.toEndec(VillagerData.CODEC));

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
            AffinityComponents.ENTITY_FLAGS.sync(villager);
            var stack = AffinityItems.VILLAGER_ARMS.getDefaultStack();
            stack.set(VILLAGER_DATA, villager.getVillagerData());
            ItemScatterer.spawn(world, villager.getX(), villager.getY(), villager.getZ(), stack);

            villager.getGossip().startGossip(player.getUuid(), VillageGossipType.MAJOR_NEGATIVE, 10);

            villager.playSound(SoundEvents.ITEM_AXE_STRIP);
            player.getStackInHand(hand).damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            return ActionResult.SUCCESS;
        });

        // damn, my conscience
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            var stack = player.getStackInHand(hand);
            if (!stack.isOf(AffinityItems.VILLAGER_ARMS)) return ActionResult.PASS;

            if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

            var data = stack.get(VILLAGER_DATA);
            var villagerData = villager.getVillagerData();

            if (data != null && !data.getProfession().equals(NITWIT)) {
                if (!data.getType().equals(villagerData.getType())) {
                    return ActionResult.PASS;
                }

                if (!data.getProfession().equals(NONE)) {
                    if (!data.getProfession().equals(villagerData.getProfession())) {
                        return ActionResult.PASS;
                    }

                    if (data.getProfession() != NITWIT && data.getLevel() < villagerData.getLevel()) {
                        return ActionResult.PASS;
                    }
                    //if (data.getLevel() == villagerData.getLevel()) {
                    //TODO "good as new" advancement
                    //}
                }
            }

            var flags = AffinityComponents.ENTITY_FLAGS.get(villager);
            if (!flags.hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS)) return ActionResult.PASS;

            // there you go, have them back
            flags.unsetFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS);
            AffinityComponents.ENTITY_FLAGS.sync(villager);
            ItemOps.decrementPlayerHandItem(player, hand);

            return ActionResult.SUCCESS;
        });
    }

    @Override
    public Text getName(ItemStack stack) {
        var data = stack.get(VILLAGER_DATA);
        if (data == null) return super.getName(stack);
        var key = "item.affinity.villager_arms";
        if (!data.getProfession().equals(NONE)) {
            key += ".with_profession";
            if (!data.getProfession().equals(NITWIT)) key += ".with_level";
        }

        var profession = Text.translatable("entity.minecraft.villager." + data.getProfession().toString().toLowerCase());
        var level = Text.translatable("merchant.level." + data.getLevel());

        return Text.translatable(key, profession, level);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        var data = stack.get(VILLAGER_DATA);
        if (data == null) return;

        tooltip.add(Text.translatable("villagerType.minecraft." + data.getType()).withColor(Colors.LIGHT_GRAY));
    }
}
