package io.wispforest.affinity.aethumflux.net;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface MultiblockAethumNetworkMember extends AethumNetworkMember {

    /**
     * @return All members of this multiblock
     */
    Collection<BlockPos> memberBlocks();

    /**
     * @return {@code true} if this member is the parent of this multiblock
     */
    boolean isParent();

}
