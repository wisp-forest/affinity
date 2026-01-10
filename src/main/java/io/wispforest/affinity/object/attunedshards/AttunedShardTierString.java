package io.wispforest.affinity.object.attunedshards;

import io.wispforest.affinity.Affinity;

import io.wispforest.endec.Endec;

import java.util.Locale;

public class AttunedShardTierString {

    public static Endec<AttunedShardTierString> ENDEC = Endec.STRING.xmap(AttunedShardTierString::new, AttunedShardTierString::getTierName);

    private String tierName;
    private AttunedShardTiers variant;

    public AttunedShardTierString(String tierName) {
        this.variant = AttunedShardTiers.valueOf(tierName.toUpperCase(Locale.ROOT));
        this.tierName = tierName;

        if (AttunedShardTiers.NONE == this.variant) {
            Affinity.LOGGER.info("Named tier {} could not be matched", tierName);
            this.tierName = "NONE";
        }
    }

    public AttunedShardTierString(AttunedShardTiers tier) {
        String name = tier.name();
        if (!name.equals("NONE")) {
            name.toLowerCase(Locale.ROOT);
        }

        this.variant = tier;
        this.tierName = name;
    }

    public String getTierName() {
        return this.tierName.toLowerCase(Locale.ROOT);
    }

    public AttunedShardTiers getTierVariant() {
        return this.variant;
    }

}
