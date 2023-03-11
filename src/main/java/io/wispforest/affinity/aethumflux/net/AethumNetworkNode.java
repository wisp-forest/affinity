package io.wispforest.affinity.aethumflux.net;

import io.wispforest.affinity.blockentity.template.LinkableBlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface AethumNetworkNode extends AethumNetworkMember {

    /**
     * Creates a link to any other network element, either a node
     * or member and also notifies that member, so it can store
     * the link parent
     *
     * @param pos  The position of the link target
     * @param type The type of link to establish
     * @return {@link io.wispforest.affinity.blockentity.template.LinkableBlockEntity.LinkResult#LINK_CREATED} if the link was established,
     * a {@link io.wispforest.affinity.blockentity.template.LinkableBlockEntity.LinkResult} describing the problem otherwise
     */
    LinkableBlockEntity.LinkResult createGenericLink(BlockPos pos, AethumLink.Type type);

    /**
     * Removes the link to the given member and notifies it,
     * given that the link exists
     *
     * @param pos The position of the link target
     * @return {@link io.wispforest.affinity.blockentity.template.LinkableBlockEntity.LinkResult#LINK_DESTROYED} if the link was destroyed,
     * a {@link io.wispforest.affinity.blockentity.template.LinkableBlockEntity.LinkResult} describing the problem otherwise
     */
    LinkableBlockEntity.LinkResult destroyLink(BlockPos pos);

    /**
     * Creates a link to another node - this specifically
     * only stores the position of the link target but
     * does not notify it
     *
     * @param pos The position of the link target
     * @return {@link io.wispforest.affinity.blockentity.template.LinkableBlockEntity.LinkResult#LINK_CREATED} if the link was established,
     * a {@link io.wispforest.affinity.blockentity.template.LinkableBlockEntity.LinkResult} describing the problem otherwise
     */
    LinkableBlockEntity.LinkResult addNodeLink(BlockPos pos);

    /**
     * Removes a link to another node - this specifically
     * only forgets the position of the link target but
     * does not notify it
     *
     * @param pos The position of the link target
     */
    void removeNodeLink(BlockPos pos);

    /**
     * @return All members linked to this node via a link of type
     * {@link AethumLink.Type#NORMAL} - this also specifically excludes other nodes
     */
    Collection<AethumNetworkMember> membersWithNormalLink();
}
