package io.wispforest.affinity.object.attunedshards;

import io.wispforest.affinity.Affinity;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;

public class CustomShardTier implements AttunedShardTier {
    public static final Endec<CustomShardTier> ENDEC = StructEndecBuilder.of(
                    Endec.LONG.fieldOf("maxTransfer", CustomShardTier::maxTransfer),
                    Endec.INT.fieldOf("maxDistance", CustomShardTier::maxDistance),
                    AttunedShardTierString.ENDEC.fieldOf("tierName", CustomShardTier::_getASTS),
                    CustomShardTier::new
            );

    private long maxTransfer;
    private int maxDistance;
    private AttunedShardTierString tierName;
    private String translationKey;

    @Environment(EnvType.CLIENT)
    private SpriteIdentifier sprite;

    private void constructHelper(long tf, int d, AttunedShardTierString tier) {
        this.maxDistance = d;
        this.maxTransfer = tf;
        this.tierName = tier;
        String tierName = tier.getTierName();
        this.translationKey = "shard_tier.affinity." + tierName;

        if (Affinity.onClient()) {
            this.sprite = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                    Affinity.id("block/" + tierName + "_aethum_flux_node_shard"));
        }
    }

    public CustomShardTier(long tf, int d, AttunedShardTierString tier) {
        this.constructHelper(tf, d, tier);
    }

    public CustomShardTier(long tf, int d, AttunedShardTiers tier) {
        AttunedShardTierString asts = new AttunedShardTierString(tier);
        this.constructHelper(tf, d, asts);
    }

    public CustomShardTier(long tf, int d, String tierName) {
        AttunedShardTierString asts = new AttunedShardTierString(tierName);
        this.constructHelper(tf, d, asts);
    }

    @Override
    public long maxTransfer() {
        return this.maxTransfer;
    }

    @Override
    public int maxDistance() {
        return this.maxDistance;
    }

    public AttunedShardTierString _getASTS() {
        return this.tierName;
    }

    public String getTierName() {
        return this.tierName.getTierName();
    }

    public AttunedShardTier getTier() {
        return this.tierName.getTierVariant();
    }

    @Override
    public String translationKey() {
        return this.translationKey;
    }

    @Environment(EnvType.CLIENT)
    public SpriteIdentifier sprite() {
        return this.sprite;
    }
}
