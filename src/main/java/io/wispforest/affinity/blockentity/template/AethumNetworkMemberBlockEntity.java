package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.aethumflux.storage.AethumFluxStorage;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.misc.BeforeMangroveBasketCaptureCallback;
import io.wispforest.affinity.misc.util.NbtUtil;
import io.wispforest.affinity.network.FluxSyncHandler;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class AethumNetworkMemberBlockEntity extends SyncedBlockEntity implements AethumNetworkMember, AethumFluxStorage.CommitCallback, InWorldTooltipProvider, BeforeMangroveBasketCaptureCallback, LinkableBlockEntity {

    public static final NbtKey<AethumLink.Element> LINK_ELEMENT_KEY = new NbtKey<>("Element", NbtKey.Type.INT.then(ordinal -> AethumLink.Element.values()[ordinal], Enum::ordinal));
    public static final NbtKey<AethumLink.Type> LINK_TYPE_KEY = new NbtKey<>("Type", NbtKey.Type.INT.then(ordinal -> AethumLink.Type.values()[ordinal], Enum::ordinal));

    @Environment(EnvType.CLIENT) private long tooltipFlux = 0;

    protected final Map<BlockPos, AethumLink.Type> links = new HashMap<>();
    protected final AethumFluxStorage fluxStorage = new AethumFluxStorage(this);

    public AethumNetworkMemberBlockEntity(BlockEntityType<? extends AethumNetworkMemberBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onBroken() {
        this.clearLinks();
    }

    protected void clearLinks() {
        for (var memberPos : this.links.keySet()) {
            var member = Affinity.AETHUM_MEMBER.find(world, memberPos, null);
            if (member == null) continue;

            member.onLinkTargetRemoved(this.pos);
        }

        this.links.clear();
    }

    @Override
    public boolean beforeMangroveBasketCapture(World world, BlockPos pos, MutableObject<BlockState> state, BlockEntity blockEntity) {
        this.clearLinks();
        return true;
    }

    @Override
    public Optional<String> beginLink(PlayerEntity player, NbtCompound linkData) {
        if (!this.acceptsLinks()) {
            return Optional.ofNullable(this instanceof AethumNetworkNode
                    ? LinkResult.TOO_MANY_LINKS.messageTranslationKey()
                    : "message.affinity.linking.cannot_be_linked");
        }

        linkData.put(LINK_ELEMENT_KEY, AethumLink.Element.of(this));
        linkData.put(LINK_TYPE_KEY, player.isSneaking() ? this.specialLinkType() : AethumLink.Type.NORMAL);
        return Optional.empty();
    }

    @Override
    public Optional<LinkResult> finishLink(PlayerEntity player, BlockPos linkTo, NbtCompound linkData) {
        if (!this.acceptsLinks()) return Optional.of(LinkResult.TOO_MANY_LINKS);

        var existingElement = linkData.get(LINK_ELEMENT_KEY);
        var linkType = linkData.get(LINK_TYPE_KEY);

        if (existingElement == AethumLink.Element.NODE) {
            var node = Affinity.AETHUM_NODE.find(this.world, linkTo, null);
            return Optional.of(node == null ? LinkResult.NO_TARGET : node.createGenericLink(this.pos, linkType));
        } else {
            return this instanceof AethumNetworkNode node
                    ? Optional.of(node.createGenericLink(linkTo, linkType))
                    : Optional.empty();
        }
    }

    @Override
    public Optional<LinkResult> destroyLink(PlayerEntity player, BlockPos destroyFrom, NbtCompound linkData) {
        var existingElement = linkData.get(LINK_ELEMENT_KEY);

        if (existingElement == AethumLink.Element.NODE) {
            var node = Affinity.AETHUM_NODE.find(world, destroyFrom, null);
            return Optional.of(node == null ? LinkResult.NO_TARGET : node.destroyLink(this.pos));
        } else {
            return this instanceof AethumNetworkNode node
                    ? Optional.of(node.destroyLink(destroyFrom))
                    : Optional.empty();
        }
    }

    @Override
    public boolean acceptsLinks() {
        return true;
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.NORMAL;
    }

    // -------------
    // Serialization
    // -------------

    @Override
    public void readNbt(NbtCompound nbt) {
        NbtUtil.readLinks(nbt, "LinkedMembers", links);
        this.fluxStorage.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtUtil.writeLinks(nbt, "LinkedMembers", links);
        this.fluxStorage.writeNbt(nbt);
    }

    @Override
    public void setStackNbt(ItemStack stack) {
        var nbt = this.createNbt();
        nbt.remove("LinkedMembers");

        BlockItem.setBlockEntityNbt(stack, this.getType(), nbt);

        // did I really just over-engineer the tooltip
        // when I was actually trying to remove the links when
        // pick-stacking aethum BEs? yes, yes I did
        //
        // glisco, 25.02.2023
        var loreList = new NbtList();
        loreList.add(NbtString.of(Text.Serializer.toJson(
                Text.empty().styled(style -> style.withItalic(false)).formatted(Formatting.DARK_GRAY)
                        .append(Text.literal("["))
                        .append(Text.literal("+").formatted(Formatting.GRAY))
                        .append(Text.literal("]"))
                        .append(Text.literal(" NBT").formatted(Formatting.GOLD))
        )));
        stack.getOrCreateSubNbt("display").put("Lore", loreList);
    }

    protected void sendFluxUpdate() {
        if (this.world.isClient) return;

        FluxSyncHandler.queueUpdate(this);
        this.world.markDirty(this.pos);
    }

    @Environment(EnvType.CLIENT)
    public void readFluxUpdate(long flux) {
        this.fluxStorage.setFlux(flux);
    }

    // -------
    // Linking
    // -------

    protected Set<BlockPos> getLinksByType(AethumLink.Type type) {
        var result = new HashSet<BlockPos>();
        for (var entry : this.links.entrySet()) {
            if (entry.getValue() != type) continue;
            result.add(entry.getKey());
        }
        return result;
    }

    @Override
    public Set<BlockPos> linkedMembers() {
        return links.keySet();
    }

    @Override
    public boolean isLinked(BlockPos pos) {
        return this.links.containsKey(pos);
    }

    @Override
    public boolean addLinkParent(BlockPos pos, AethumLink.Type type) {
        if (isLinked(pos)) return false;

        this.links.put(pos.toImmutable(), type);
        this.markDirty(true);

        return true;
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        this.links.remove(pos);
        this.markDirty(true);
    }

    // ------------
    // Flux methods
    // ------------

    @Override
    public void updateTooltipEntries(boolean force, float delta) {
        if (force) {
            this.tooltipFlux = this.displayFlux();
            return;
        }

        if (this.displayFlux() != this.tooltipFlux) {
            float diff = Delta.compute(this.tooltipFlux, this.displayFlux(), delta * .5f);
            this.tooltipFlux += Math.signum(diff) * Math.max(1, Math.abs(diff));
        }
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        entries.add(Entry.icon(Text.of(String.valueOf(this.tooltipFlux)), 0, 0));
    }

    public void updateFlux(long flux) {
        if (this.fluxStorage.setFlux(flux)) this.sendFluxUpdate();
    }

    /**
     * @return The amount of flux to display
     * when looking at this member. Potentially different
     * from the true stored amount
     */
    public long displayFlux() {
        return this.flux();
    }

    @Override
    public long flux() {
        return this.fluxStorage.flux();
    }

    /**
     * @return The flux capacity to display
     * when looking at this member. Potentially different
     * from the true capacity
     */
    public long displayFluxCapacity() {
        return this.fluxCapacity();
    }

    @Override
    public long fluxCapacity() {
        return this.fluxStorage.fluxCapacity();
    }

    @Override
    public long insert(long max, TransactionContext transaction) {
        return this.fluxStorage.insert(max, transaction);
    }

    @Override
    public boolean canInsert() {
        return this.fluxStorage.canInsert();
    }

    @Override
    public long maxInsert() {
        return this.fluxStorage.maxInsert();
    }

    @Override
    public long extract(long max, TransactionContext transaction) {
        return this.fluxStorage.extract(max, transaction);
    }

    @Override
    public long maxExtract() {
        return this.fluxStorage.maxExtract();
    }

    @Override
    public boolean canExtract() {
        return this.fluxStorage.canExtract();
    }

    @Override
    public void onTransactionCommitted() {
        this.sendFluxUpdate();
    }
}
