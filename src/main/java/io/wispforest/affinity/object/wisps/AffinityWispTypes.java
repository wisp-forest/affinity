package io.wispforest.affinity.object.wisps;

public enum AffinityWispTypes implements WispType {

    INERT(0x548CFF, "⭘"),
    WISE(0x2EB086, "☀"),
    VICIOUS(0xB8405E, "🗡");

    private final int color;
    private final String translationKey;
    private final String icon;

    AffinityWispTypes(int color, String icon) {
        this.color = color;
        this.icon = icon;
        this.translationKey = "wispType.affinity." + this.name().toLowerCase();
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
}
