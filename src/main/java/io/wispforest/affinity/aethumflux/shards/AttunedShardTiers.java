package io.wispforest.affinity.aethumflux.shards;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.AttunedShardItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

public enum AttunedShardTiers implements AttunedShardTier {

    NONE(0),
    CRUDE(50),
    MILDLY_ATTUNED(100),
    FAIRLY_ATTUNED(500),
    GREATLY_ATTUNED(2000);

    private final long maxTransfer;
    private final String translationKey;

    @Environment(EnvType.CLIENT)
    private SpriteIdentifier sprite;

    AttunedShardTiers(int maxTransfer) {
        this.maxTransfer = maxTransfer;

        final var name = this.name().toLowerCase();
        this.translationKey = "shard_tier.affinity." + name;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.sprite = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                    Affinity.id("block/" + name + "_aethum_flux_node_shard"));
        }
    }

    @NotNull
    public static AttunedShardTier forItem(Item item) {
        if (item instanceof AttunedShardItem shardItem) return shardItem.tier();
        if (item == Items.AMETHYST_SHARD) return AttunedShardTiers.CRUDE;
        return AttunedShardTiers.NONE;
    }

    @Override
    public long maxTransfer() {
        return maxTransfer;
    }

    @Override
    public String translationKey() {
        return translationKey;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public SpriteIdentifier sprite() {
        return sprite;
    }
}
