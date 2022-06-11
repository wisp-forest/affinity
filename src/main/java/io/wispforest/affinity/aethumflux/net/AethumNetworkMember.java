package io.wispforest.affinity.aethumflux.net;

import io.wispforest.affinity.aethumflux.storage.AethumFluxContainer;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface AethumNetworkMember extends AethumFluxContainer {

    /**
     * Sets the member's flux amount to {@code flux}.
     */
    void updateFlux(long flux);

    /**
     * @return The positions of all network members
     */
    Set<BlockPos> linkedMembers();

    /**
     * @return {@code true} if this member is linked to another member at {@code pos}
     */
    boolean isLinked(BlockPos pos);

    /**
     * @return {@code true} if a new link to this member can be established.
     * <b>On a node, this method must only return false if the connection limit is reached</b>
     */
    boolean acceptsLinks();

    /**
     * Adds a link parent to this member, so that it can notify
     * all it's parents when it is removed
     *
     * @param pos  The parent position
     * @param type The type of link that was established
     * @return {@code true} if the link parent was successfully saved
     */
    boolean addLinkParent(BlockPos pos, AethumLink.Type type);

    /**
     * Called when any member this one is linked to get removed
     *
     * @param pos The position of the member that was removed
     */
    void onLinkTargetRemoved(BlockPos pos);

    /**
     * @return The type of link to establish if the player is pressing
     * {@code SHIFT} while beginning the linking process
     */
    AethumLink.Type specialLinkType();
}
