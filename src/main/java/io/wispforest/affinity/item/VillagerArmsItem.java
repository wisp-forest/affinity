package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.VillagerArmatureBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.village.VillageGossipType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static net.minecraft.village.VillagerProfession.NITWIT;
import static net.minecraft.village.VillagerProfession.NONE;

public class VillagerArmsItem extends Item {

    public VillagerArmsItem(Settings settings) {
        super(settings);
    }

    static {
        // let's go steal some villager arms
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!Affinity.config().unfinishedFeatures()) return ActionResult.PASS;

            if (!player.shouldCancelInteraction() || !player.getStackInHand(hand).isIn(ItemTags.AXES)) return ActionResult.PASS;
            if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

            var flags = AffinityComponents.ENTITY_FLAGS.get(villager);
            if (flags.hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS)) return ActionResult.PASS;

            // ha! gottem
            flags.setFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS);
            AffinityComponents.ENTITY_FLAGS.sync(villager);
            var stack = AffinityItems.VILLAGER_ARMS.getDefaultStack();
            stack.put(VillagerArmatureBlockEntity.VILLAGER_DATA, villager.getVillagerData());
            ItemScatterer.spawn(world, villager.getX(), villager.getY(), villager.getZ(), stack);

            villager.getGossip().startGossip(player.getUuid(), VillageGossipType.MAJOR_NEGATIVE, 10);

            villager.playSoundIfNotSilent(AffinitySoundEvents.ENTITY_VILLAGER_STRIP_ARMS);
            player.getStackInHand(hand).damage(1, player, playerEntity -> playerEntity.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));

            return ActionResult.SUCCESS;
        });

        // damn, my conscience
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!Affinity.config().unfinishedFeatures()) return ActionResult.PASS;

            var stack = player.getStackInHand(hand);
            if (!stack.isOf(AffinityItems.VILLAGER_ARMS)) return ActionResult.PASS;

            if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

            var data = stack.get(VillagerArmatureBlockEntity.VILLAGER_DATA);
            var villagerData = villager.getVillagerData();

            if (data != null && !data.getProfession().equals(NITWIT)) {
                if (!data.getType().equals(villagerData.getType())) {
                    return ActionResult.PASS;
                }

                if (!data.getProfession().equals(NONE)) {
                    if (!data.getProfession().equals(villagerData.getProfession())) {
                        return ActionResult.PASS;
                    }

                    if (data.getLevel() < villagerData.getLevel()) {
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
        var data = stack.get(VillagerArmatureBlockEntity.VILLAGER_DATA);
        if (data == null) return super.getName(stack);
        var key = "item.affinity.villager_arms";
        if (!data.getProfession().equals(NONE)) {
            key += ".with_profession";
            if (!data.getProfession().equals(NITWIT)) key += ".with_level";
        }

        var profession = Text.translatable("entity.minecraft.villager." + data.getProfession().toString().toLowerCase(Locale.ROOT));
        var level = Text.translatable("merchant.level." + data.getLevel());

        return Text.translatable(key, profession, level);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        var data = stack.get(VillagerArmatureBlockEntity.VILLAGER_DATA);
        if (data == null) return;

        tooltip.add(Text.translatable("villagerType.minecraft." + data.getType()).styled(style -> style.withColor(Formatting.GRAY)));
    }
}
