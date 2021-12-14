package io.wispforest.affinity.aethumflux.net;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface AethumNetworkNode extends AethumNetworkMember {

    /**
     * Creates a link to any other network element, either a node
     * or member and also notifies that member, so it can store
     * the link parent
     *
     * @param pos The position of the link target
     * @param type The type of link to establish
     * @return {@link AethumLink.Result#SUCCESS} if the link was established,
     * a {@link AethumLink.Result} describing the problem otherwise
     */
    AethumLink.Result createGenericLink(BlockPos pos, AethumLink.Type type);

    /**
     * Creates a link to another node - this specifically
     * only stores the position of the link target but
     * does not notify it
     *
     * @param pos The position of the link target
     */
    void addNodeLink(BlockPos pos);

    /**
     * @return All members linked to this node - this specifically excludes other nodes
     */
    Collection<AethumNetworkMember> getLinkedMembers();
}
