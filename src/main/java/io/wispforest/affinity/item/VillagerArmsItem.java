package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

import java.util.List;

import static net.minecraft.village.VillagerProfession.NITWIT;
import static net.minecraft.village.VillagerProfession.NONE;

public class VillagerArmsItem extends Item {

    public static final ComponentType<ArmsData> VILLAGER_DATA = Affinity.component(
        "villager_data",
        CodecUtils.toEndecWithRegistries(
            VillagerData.CODEC.xmap(ArmsData::wrap, ArmsData::unwrap),
            VillagerData.PACKET_CODEC.xmap(ArmsData::wrap, ArmsData::unwrap)
        )
    );

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
            stack.set(VILLAGER_DATA, ArmsData.wrap(villager.getVillagerData()));
            ItemScatterer.spawn(world, villager.getX(), villager.getY(), villager.getZ(), stack);

            villager.getGossip().startGossip(player.getUuid(), VillageGossipType.MAJOR_NEGATIVE, 10);

            villager.playSound(AffinitySoundEvents.ENTITY_VILLAGER_STRIP_ARMS);
            player.getStackInHand(hand).damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            return ActionResult.SUCCESS;
        });

        // damn, my conscience
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!Affinity.config().unfinishedFeatures()) return ActionResult.PASS;

            var stack = player.getStackInHand(hand);
            if (!stack.isOf(AffinityItems.VILLAGER_ARMS)) return ActionResult.PASS;

            if (!(entity instanceof VillagerEntity villager)) return ActionResult.PASS;

            var data = stack.get(VILLAGER_DATA);
            var villagerData = villager.getVillagerData();

            if (data != null && !data.profession().equals(NITWIT)) {
                if (!data.type().equals(villagerData.getType())) {
                    return ActionResult.PASS;
                }

                if (!data.profession().equals(NONE)) {
                    if (!data.profession().equals(villagerData.getProfession())) {
                        return ActionResult.PASS;
                    }

                    if (data.level() < villagerData.getLevel()) {
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
        if (!data.profession().equals(NONE)) {
            key += ".with_profession";
            if (!data.profession().equals(NITWIT)) key += ".with_level";
        }

        var profession = Text.translatable("entity.minecraft.villager." + data.profession().toString().toLowerCase());
        var level = Text.translatable("merchant.level." + data.level());

        return Text.translatable(key, profession, level);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        var data = stack.get(VILLAGER_DATA);
        if (data == null) return;

        tooltip.add(Text.translatable("villagerType.minecraft." + data.type()).withColor(Colors.LIGHT_GRAY));
    }

    public record ArmsData(VillagerType type, VillagerProfession profession, int level) {
        public static ArmsData wrap(VillagerData data) {
            return new ArmsData(data.getType(), data.getProfession(), data.getLevel());
        }

        public VillagerData unwrap() {
            return new VillagerData(this.type, this.profession, this.level);
        }
    }
}
