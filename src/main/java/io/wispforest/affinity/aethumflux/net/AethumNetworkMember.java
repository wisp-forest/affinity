package io.wispforest.affinity.aethumflux.net;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.storage.AethumFluxContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public interface AethumNetworkMember extends AethumFluxContainer {
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
     * all its parents when it is removed
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
     * @return The type of link to establish if the player is
     * sneaking while beginning the linking process
     */
    AethumLink.Type specialLinkType();

    /**
     * @return An offset from the center of this block
     * at which aethum links should attach
     */
    default Vec3d linkAttachmentPointOffset() {
        return Vec3d.ZERO;
    }

    static Set<BlockPos> traverseNetwork(World world, BlockPos initialMember) {
        return traverseNetwork(world, initialMember, (aethumNetworkMember, aBoolean) -> {});
    }

    /**
     * Perform a BFS on the network starting with the given
     * member position in the given world
     *
     * @param world         The world to search in
     * @param initialMember The member to start searching at
     * @param callback      A callback to invoke every time a network member is discovered.
     *                      The boolean parameter is {@code true} if the member parameter
     *                      was discovered as a multiblock child
     * @return A mutable set containing the positions of all network members
     */
    static Set<BlockPos> traverseNetwork(World world, BlockPos initialMember, BiConsumer<AethumNetworkMember, Boolean> callback) {
        var members = new HashSet<BlockPos>();

        var queue = new ArrayDeque<BlockPos>();
        queue.add(initialMember);

        while (!queue.isEmpty()) {
            var memberPos = queue.poll();

            var peer = Affinity.AETHUM_MEMBER.find(world, memberPos, null);
            if (peer == null) continue;

            members.add(memberPos);
            if (peer instanceof MultiblockAethumNetworkMember multiblock) {
                for (var multiblockMemberPos : multiblock.memberBlocks()) {
                    if (multiblockMemberPos.equals(memberPos)) continue;

                    members.add(multiblockMemberPos);

                    var multiblockMember = Affinity.AETHUM_MEMBER.find(world, multiblockMemberPos, null);
                    if (multiblockMember == null) continue;

                    callback.accept(multiblockMember, true);
                }
            }

            callback.accept(peer, false);

            for (var neighbor : peer.linkedMembers()) {
                if (members.contains(neighbor) || queue.contains(neighbor)) continue;
                queue.add(neighbor);
            }
        }

        return members;
    }
}
