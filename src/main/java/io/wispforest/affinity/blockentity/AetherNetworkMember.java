package io.wispforest.affinity.blockentity;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface AetherNetworkMember {

    long flux();

    List<BlockPos> linkedMembers();

    boolean isLinked(BlockPos pos);

    boolean addLinkParent(BlockPos pos);

    void onLinkTargetRemoved(BlockPos pos);
}
