package io.wispforest.affinity.aethumflux.shards;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.SpriteIdentifier;

/**
 * The tier of a shard that can be used inside the
 * copper plated aethum flux node
 */
public interface AttunedShardTier {

    /**
     * @return The maximum amount of flux a node with a shard
     * of this tier can transfer per connection per tick
     */
    long maxTransfer();

    /**
     * @return The translation key of this tier, used in the
     * HUD when looking at a node with a shard of this tier
     */
    String translationKey();

    /**
     * @return The sprite to use when rendering a node
     * with a shard of this tier on it
     */
    @Environment(EnvType.CLIENT)
    SpriteIdentifier sprite();

}
