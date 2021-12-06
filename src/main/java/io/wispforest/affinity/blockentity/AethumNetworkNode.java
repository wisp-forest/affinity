package io.wispforest.affinity.blockentity;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface AethumNetworkNode extends AethumNetworkMember {

    AethumLink.Result createGenericLink(BlockPos pos);

    Collection<AethumNetworkMember> getConnectedMembers();

    void addNodeLink(BlockPos pos);

    boolean canLink(BlockPos pos);

}
