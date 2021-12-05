package io.wispforest.affinity.blockentity;

import net.minecraft.util.math.BlockPos;

public interface AetherNetworkNode extends AetherNetworkMember {

    AetherLink.Result createGenericLink(BlockPos pos);

    void addNodeLink(BlockPos pos);

    boolean canLink(BlockPos pos);

}
