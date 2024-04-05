package io.wispforest.affinity.blockentity.impl;

import com.google.common.collect.Iterables;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.screen.EtherealAethumFluxInjectorScreen;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.annotations.NullableComponent;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EtherealAethumFluxInjectorBlockEntity extends BlockEntity implements InteractableBlockEntity, TickedBlockEntity {

    private static final KeyedEndec<BlockPos> LAST_KNOWN_SOURCE_NODE_KEY = BuiltInEndecs.BLOCK_POS.keyed("last_known_source_node", (BlockPos) null);
    private static final KeyedEndec<Long> LAST_INSERTION_TIMESTAMP_KEY = Endec.LONG.keyed("last_insertion_timestamp", 0L);

    @Environment(EnvType.CLIENT) public Vector4f particle1Offset;
    @Environment(EnvType.CLIENT) public Vector4f particle2Offset;

    private @Nullable BlockPos lastKnownSourceNode = null;
    private long lastInsertionTimestamp = 0L;

    public EtherealAethumFluxInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ETHEREAL_AETHUM_FLUX_INJECTOR, pos, state);
    }

    public boolean canInsert() {
        return this.world.getTime() - this.lastInsertionTimestamp > 20;
    }

    public void postInsert() {
        this.lastInsertionTimestamp = this.world.getTime();
        this.markDirty();
    }

    @Override
    public void tickClient() {
        if (this.particle1Offset == null || this.particle2Offset == null) return;

        ClientParticles.spawn(new DustParticleEffect(MathUtil.rgbToVec3f(0x5b93cc), .5f), this.world, Vec3d.of(this.pos).add(this.particle1Offset.x, this.particle1Offset.y, this.particle1Offset.z), 0);
        ClientParticles.spawn(new DustParticleEffect(MathUtil.rgbToVec3f(0x5bbccc), .5f), this.world, Vec3d.of(this.pos).add(this.particle2Offset.x, this.particle2Offset.y, this.particle2Offset.z), 0);
        this.particle1Offset = this.particle2Offset = null;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!this.world.isClient) {
            var storage = this.world.getScoreboard().getComponent(AffinityComponents.ETHEREAL_NODE_STORAGE);

            if (this.lastKnownSourceNode != null
                    && (storage.listInjectors(this.lastKnownSourceNode) == null
                    || !storage.listInjectors(this.lastKnownSourceNode).contains(this.pos))) {
                this.lastKnownSourceNode = null;
                this.markDirty();
            }

            var globalNodes = storage.listGlobalNodes().toList();
            var privateNodes = storage.listNodes(player.getUuid()).toList();
            var nodeNames = new HashMap<BlockPos, Text>();

            for (var nodePos : Iterables.concat(globalNodes, privateNodes)) {
                var name = storage.nodeName(nodePos);
                if (name != null) nodeNames.put(nodePos, name);
            }

            AffinityNetwork.server(player).send(new OpenScreenPacket(
                    this.pos,
                    globalNodes,
                    privateNodes,
                    nodeNames,
                    this.lastKnownSourceNode
            ));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putIfNotNull(LAST_KNOWN_SOURCE_NODE_KEY, this.lastKnownSourceNode);
        nbt.put(LAST_INSERTION_TIMESTAMP_KEY, this.lastInsertionTimestamp);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.lastKnownSourceNode = nbt.get(LAST_KNOWN_SOURCE_NODE_KEY);
        this.lastInsertionTimestamp = nbt.get(LAST_INSERTION_TIMESTAMP_KEY);
    }

    public BlockPos lastKnownSourceNode() {
        return this.lastKnownSourceNode;
    }

    public record OpenScreenPacket(
            BlockPos injectorPos,
            List<BlockPos> globalNodes,
            List<BlockPos> privateNodes,
            Map<BlockPos, Text> nodeNames,
            @NullableComponent BlockPos currentNode
    ) {}

    public record SetInjectorNodePacket(BlockPos injectorPos, BlockPos nodePos) {}

    public static void initNetwork() {
        //noinspection Convert2MethodRef
        AffinityNetwork.CHANNEL.registerClientbound(OpenScreenPacket.class, (message, access) -> handleOpenScreenPacket(message, access));

        AffinityNetwork.CHANNEL.registerServerbound(SetInjectorNodePacket.class, (message, access) -> {
            if (message.injectorPos.getSquaredDistance(access.player().getPos()) > 100
                    || !(access.player().getWorld().getBlockEntity(message.injectorPos) instanceof EtherealAethumFluxInjectorBlockEntity injector)) {
                return;
            }

            var storage = access.player().getWorld().getScoreboard().getComponent(AffinityComponents.ETHEREAL_NODE_STORAGE);
            storage.addInjector(message.nodePos, message.injectorPos);

            if (injector.lastKnownSourceNode != null) {
                storage.removeInjector(injector.lastKnownSourceNode, message.injectorPos);
            }

            injector.lastKnownSourceNode = message.nodePos;
            injector.markDirty();
        });
    }

    @Environment(EnvType.CLIENT)
    private static void handleOpenScreenPacket(OpenScreenPacket message, ClientAccess access) {
        access.runtime().setScreen(new EtherealAethumFluxInjectorScreen(message.injectorPos, message.globalNodes, message.privateNodes, message.nodeNames, message.currentNode));
    }
}