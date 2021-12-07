package io.wispforest.affinity.util.aethumflux;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface AethumNetworkMember extends AethumFluxContainer {

    /**
     * @return The positions of all network members
     */
    List<BlockPos> linkedMembers();

    /**
     * @return {@code true} if this member is linked to another member at {@code pos}
     */
    boolean isLinked(BlockPos pos);

    /**
     * Adds a link parent to this member, so that it can notify
     * all it's parents when it is removed
     *
     * @param pos The parent position
     * @return {@code true} if the link parent was successfully saved
     */
    boolean addLinkParent(BlockPos pos);

    /**
     * Called when any member this one is linked to get removed
     *
     * @param pos The position of the member that was removed
     */
    void onLinkTargetRemoved(BlockPos pos);
}
