package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.EtherealAethumFluxInjectorBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.List;
import java.util.UUID;

public class EtherealAethumFluxNodeBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, Nameable {

    private static final KeyedEndec<Text> CUSTOM_NAME_KEY = Endec.STRING.<Text>xmap(Text.Serializer::fromJson, Text.Serializer::toJson).keyed("custom_name", (Text) null);
    private static final KeyedEndec<UUID> OWNER_KEY = BuiltInEndecs.UUID.keyed("owner", (UUID) null);
    private static final KeyedEndec<Boolean> GLOBAL_KEY = Endec.BOOLEAN.keyed("global", false);
    private static final KeyedEndec<ItemStack> SHARD = io.wispforest.affinity.endec.BuiltInEndecs.ITEM_STACK.keyed("shard", ItemStack.EMPTY);

    @Environment(EnvType.CLIENT) public Vector4f particle1Offset;
    @Environment(EnvType.CLIENT) public Vector4f particle2Offset;

    private @Nullable Text customName = null;
    private @Nullable UUID owner = null;
    private boolean global = false;

    private ItemStack shard = ItemStack.EMPTY;
    private boolean hasVoidShard = false;

    public EtherealAethumFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ETHEREAL_AETHUM_FLUX_NODE, pos, state);

        this.fluxStorage.setFluxCapacity(16000);
        this.fluxStorage.setMaxInsert(4000);
    }

    @Override
    public void tickClient() {
        if (this.particle1Offset == null || this.particle2Offset == null) return;

        ClientParticles.spawn(new DustParticleEffect(MathUtil.rgbToVec3f(0x5b93cc), .5f), this.world, Vec3d.of(this.pos).add(this.particle1Offset.x, this.particle1Offset.y, this.particle1Offset.z), 0);
        ClientParticles.spawn(new DustParticleEffect(MathUtil.rgbToVec3f(0x5bbccc), .5f), this.world, Vec3d.of(this.pos).add(this.particle2Offset.x, this.particle2Offset.y, this.particle2Offset.z), 0);
        this.particle1Offset = this.particle2Offset = null;
    }

    @Override
    public void tickServer() {
        if (!this.hasShard() || this.world.isReceivingRedstonePower(this.pos)) return;

        final var globalNodePos = GlobalPos.create(this.world.getRegistryKey(), this.pos);

        var storage = this.world.getScoreboard().getComponent(AffinityComponents.ETHEREAL_NODE_STORAGE);
        storage.addNode(globalNodePos, this.owner, this.customName, this.global);

        var injectors = storage.listInjectors(globalNodePos);
        if (injectors == null) return;

        var transferPerInjector = this.hasVoidShard
            ? this.flux() / injectors.size()
            : 10;

        for (var injectorPos : injectors) {
            var world = ((ServerWorld) this.world).getServer().getWorld(injectorPos.getDimension());
            if (world == null) continue;

            if (!world.isChunkLoaded(ChunkSectionPos.getSectionCoord(injectorPos.getPos().getX()), ChunkSectionPos.getSectionCoord(injectorPos.getPos().getZ()))) {
                continue;
            }

            var be = world.getBlockEntity(injectorPos.getPos());
            if (!(be instanceof EtherealAethumFluxInjectorBlockEntity injector) || (!this.hasVoidShard && !injector.isPastCooldown())) continue;

            var attachedMember = Affinity.AETHUM_MEMBER.find(world, injectorPos.getPos().offset(injector.getCachedState().get(EtherealAethumFluxInjectorBlock.FACING)), null);
            if (attachedMember == null) continue;

            try (var transaction = Transaction.openOuter()) {
                var inserted = attachedMember.insert(Math.min(transferPerInjector, this.flux()), transaction);

                if (inserted > 0) {
                    transaction.commit();
                    this.updateFlux(this.flux() - inserted);

                    injector.postInsert();
                }
            }

            if (this.flux() == 0) return;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        if (this.hasCustomName()) {
            entries.add(1, Entry.icon(this.getDisplayName(), 0, 8));
        }

        if (MinecraftClient.getInstance().player.getUuid().equals(this.owner)) {
            entries.add(Entry.icon(
                Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".tooltip." + (this.global ? "visibility_public" : "visibility_private")),
                this.global ? 16 : 8, 8
            ));

            entries.add(Entry.text(
                Text.empty(),
                Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".tooltip.owned_by_you")
            ));
        } else {
            entries.add(Entry.text(
                Text.empty(),
                Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".tooltip.owned_by_someone_else")
            ));
        }
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.markDirty();
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

    @Nullable
    @Override
    public Text getName() {
        return this.customName != null ? this.customName : this.getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public Text getCustomName() {
        return this.customName;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putIfNotNull(SerializationContext.empty(), CUSTOM_NAME_KEY, this.customName);
        nbt.putIfNotNull(SerializationContext.empty(), OWNER_KEY, this.owner);
        nbt.put(GLOBAL_KEY, this.global);
        nbt.put(SHARD, this.shard);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.customName = nbt.get(CUSTOM_NAME_KEY);
        this.owner = nbt.get(OWNER_KEY);
        this.global = nbt.get(GLOBAL_KEY);
        this.setShardStack(nbt.get(SHARD));
    }

    public void setShardStack(@NotNull ItemStack shardStack) {
        this.shard = shardStack;
        this.hasVoidShard = this.shard.isOf(AffinityItems.VOID_RESONANT_ETHEREAL_AMETHYST_SHARD);
    }

    public boolean hasShard() {
        return !this.shard.isEmpty();
    }

    public boolean hasVoidShard() {
        return this.hasVoidShard;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getUuid().equals(this.owner)) return ActionResult.PASS;

        if (player.isSneaking()) {
            this.global = !this.global;
            this.markDirty();

            return ActionResult.SUCCESS;
        } else {
            var playerStack = player.getStackInHand(hand);
            if (playerStack.isEmpty()) {
                if (this.shard.isEmpty()) return ActionResult.PASS;

                player.setStackInHand(hand, this.shard);
                this.setShardStack(ItemStack.EMPTY);
            } else {
                if (this.shard.isEmpty()) {
                    if (!playerStack.isOf(AffinityItems.VOID_RESONANT_ETHEREAL_AMETHYST_SHARD) && !playerStack.isOf(AffinityItems.SCULK_RESONANT_ETHEREAL_AMETHYST_SHARD)) {
                        return ActionResult.PASS;
                    }

                    this.setShardStack(ItemOps.singleCopy(playerStack));
                    ItemOps.decrementPlayerHandItem(player, hand);
                } else {
                    if (ItemOps.canStack(playerStack, this.shard)) {
                        playerStack.increment(1);
                    } else {
                        player.getInventory().insertStack(this.shard);
                    }

                    this.setShardStack(ItemStack.EMPTY);
                }
            }

            this.markDirty();
            return ActionResult.SUCCESS;
        }
    }
}
