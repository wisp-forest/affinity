package io.wispforest.affinity.object.wisps;

public enum AffinityWispTypes implements WispType {

    INERT(0x548CFF, "⭘", 15),
    WISE(0x2EB086, "☀", 25),
    VICIOUS(0xB8405E, "🗡", 50);

    private final int color;
    private final String translationKey;
    private final String icon;
    private final int aethumFluxPerSecond;

    AffinityWispTypes(int color, String icon, int aethumFluxPerSecond) {
        this.color = color;
        this.icon = icon;
        this.translationKey = "wispType.affinity." + this.name().toLowerCase();
        this.aethumFluxPerSecond = aethumFluxPerSecond;
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
}
