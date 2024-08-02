package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WispSpawnEggItem extends SpawnEggItem {

    private static final Text TRANSLATED_NAME = Text.translatable(Util.createTranslationKey("item", Affinity.id("wisp_spawn_egg")));
    private final WispType type;

    public WispSpawnEggItem(EntityType<? extends MobEntity> type, WispType wispType) {
        super(type, wispType.color(), Util.make(() -> {
            var hsv = Color.ofRgb(wispType.color()).hsv();
            return Color.ofHsv(hsv[0], hsv[1], hsv[2] * .65f).rgb();
        }), new Item.Settings());

        this.type = wispType;
    }

    @Override
    public Text getName() {
        return TRANSLATED_NAME;
    }

    @Override
    public Text getName(ItemStack stack) {
        return TRANSLATED_NAME;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(this.type.createTooltip());
    }

    static {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(AffinityItems.INERT_WISP_SPAWN_EGG);
            entries.add(AffinityItems.WISE_WISP_SPAWN_EGG);
            entries.add(AffinityItems.VICIOUS_WISP_SPAWN_EGG);
        });
    }
}
