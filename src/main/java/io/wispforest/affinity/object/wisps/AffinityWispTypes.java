package io.wispforest.affinity.object.wisps;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.item.Item;

import java.util.Locale;
import java.util.function.Supplier;

public enum AffinityWispTypes implements WispType {

    INERT(0x548CFF, "⭘", 15, () -> AffinityItems.INERT_WISP_MIST),
    WISE(0x2EB086, "☀", 25, () -> AffinityItems.WISE_WISP_MIST),
    VICIOUS(0xB8405E, "🗡", 50, () -> AffinityItems.VICIOUS_WISP_MIST);

    private final int color;
    private final String translationKey;
    private final String icon;
    private final int aethumFluxPerSecond;
    private final Supplier<Item> mistItem;

    AffinityWispTypes(int color, String icon, int aethumFluxPerSecond, Supplier<Item> mistItem) {
        this.color = color;
        this.icon = icon;
        this.translationKey = "wispType.affinity." + this.name().toLowerCase(Locale.ROOT);
        this.aethumFluxPerSecond = aethumFluxPerSecond;
        this.mistItem = mistItem;
    }

    @Override
    public int color() {
        return this.color;
    }

    @Override
    public String translationKey() {
        return this.translationKey;
    }

    @Override
    public String icon() {
        return this.icon;
    }

    @Override
    public int aethumFluxPerSecond() {
        return this.aethumFluxPerSecond;
    }

    @Override
    public Item mistItem() {
        return this.mistItem.get();
    }
}
