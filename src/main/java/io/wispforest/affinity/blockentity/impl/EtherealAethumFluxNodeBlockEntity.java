package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.EtherealAethumFluxInjectorBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.List;
import java.util.UUID;

public class EtherealAethumFluxNodeBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, Nameable {

    private static final KeyedEndec<Text> CUSTOM_NAME_KEY = Endec.ofCodec(TextCodecs.STRINGIFIED_CODEC).keyed("custom_name", (Text) null);
    private static final KeyedEndec<UUID> OWNER_KEY = BuiltInEndecs.UUID.keyed("owner", (UUID) null);
    private static final KeyedEndec<Boolean> GLOBAL_KEY = Endec.BOOLEAN.keyed("global", false);

    @Environment(EnvType.CLIENT) public Vector4f particle1Offset;
    @Environment(EnvType.CLIENT) public Vector4f particle2Offset;

    private @Nullable Text customName = null;
    private @Nullable UUID owner = null;
    private boolean global = false;

    public EtherealAethumFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ETHEREAL_AETHUM_FLUX_NODE, pos, state);

        this.fluxStorage.setFluxCapacity(16000);
        this.fluxStorage.setMaxInsert(128);
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
        var storage = this.world.getScoreboard().getComponent(AffinityComponents.ETHEREAL_NODE_STORAGE);
        storage.addNode(this.pos, this.owner, this.customName, this.global);

        var injectors = storage.listInjectors(this.pos);
        if (injectors == null) return;

        for (var injectorPos : injectors) {
            if (!this.world.isChunkLoaded(ChunkSectionPos.getSectionCoord(injectorPos.getX()), ChunkSectionPos.getSectionCoord(injectorPos.getZ()))) {
                continue;
            }

            var be = this.world.getBlockEntity(injectorPos);
            if (!(be instanceof EtherealAethumFluxInjectorBlockEntity injector) || !injector.canInsert()) continue;

            var attachedMember = Affinity.AETHUM_MEMBER.find(world, injectorPos.offset(injector.getCachedState().get(EtherealAethumFluxInjectorBlock.FACING)), null);
            if (attachedMember == null) continue;

            try (var transaction = Transaction.openOuter()) {
                var inserted = attachedMember.insert(Math.min(10, this.flux()), transaction);

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
            entries.add(0, Entry.icon(this.getDisplayName(), 0, 8));
        }

        if (MinecraftClient.getInstance().player.getUuid().equals(this.owner)) {
            entries.add(Entry.icon(
                    this.global ? Text.literal("Accessible to everyone") : Text.literal("Accessible only to you"),
                    this.global ? 16 : 8, 8
            ));

            entries.add(Entry.text(
                    Text.empty(),
                    Text.literal("Owned by you")
            ));
        } else {
            entries.add(Entry.text(
                    Text.empty(),
                    Text.literal("Owned by someone else")
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
        nbt.putIfNotNull(CUSTOM_NAME_KEY, this.customName);
        nbt.putIfNotNull(OWNER_KEY, this.owner);
        nbt.put(GLOBAL_KEY, this.global);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.customName = nbt.get(CUSTOM_NAME_KEY);
        this.owner = nbt.get(OWNER_KEY);
        this.global = nbt.get(GLOBAL_KEY);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) return ActionResult.PASS;

        this.global = !this.global;
        this.markDirty();

        return ActionResult.SUCCESS;
    }
}
