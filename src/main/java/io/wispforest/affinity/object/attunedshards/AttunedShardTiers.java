package io.wispforest.affinity.object.attunedshards;

import io.wispforest.affinity.Affinity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;

public enum AttunedShardTiers implements AttunedShardTier {

    NONE(0, 5),
    CRUDE(5, 5),
    MILDLY_ATTUNED(15, 10),
    FAIRLY_ATTUNED(75, 15),
    GREATLY_ATTUNED(300, 20);

    private final long maxTransfer;
    private final int maxDistance;
    private final String translationKey;

    @Environment(EnvType.CLIENT)
    private SpriteIdentifier sprite;

    AttunedShardTiers(int maxTransfer, int maxDistance) {
        this.maxTransfer = maxTransfer;
        this.maxDistance = maxDistance;

        final var name = this.name().toLowerCase();
        this.translationKey = "shard_tier.affinity." + name;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.sprite = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                    Affinity.id("block/" + name + "_aethum_flux_node_shard"));
        }
    }

    @Override
    public long maxTransfer() {
        return this.maxTransfer;
    }

    @Override
    public int maxDistance() {
        return this.maxDistance;
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
