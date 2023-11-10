package io.wispforest.affinity.entity;

import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.owo.nbt.NbtKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EmancipatedBlockEntity extends Entity {

    private static final TrackedDataHandler<Optional<NbtCompound>> OPTIONAL_NBT = TrackedDataHandler.ofOptional(PacketByteBuf::writeNbt, PacketByteBuf::readNbt);
    static {
        TrackedDataHandlerRegistry.register(OPTIONAL_NBT);
    }

    private static final NbtKey<Integer> MAX_AGE_KEY = new NbtKey<>("max_age", NbtKey.Type.INT);
    private static final NbtKey<NbtCompound> EMANCIPATED_BLOCK_ENTITY_DATA_KEY = new NbtKey<>("emancipated_block_entity", NbtKey.Type.COMPOUND);
    private static final NbtKey<BlockState> EMANCIPATED_STATE_KEY = new NbtKey<>(
            "emancipated_state",
            NbtKey.Type.COMPOUND.then(nbt -> NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), nbt), NbtHelper::fromBlockState)
    );

    @Nullable
    @Environment(EnvType.CLIENT)
    public BlockEntity renderBlockEntity = null;

    private static final TrackedData<Integer> MAX_AGE = DataTracker.registerData(EmancipatedBlockEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<NbtCompound>> EMANCIPATED_BLOCK_ENTITY_DATA = DataTracker.registerData(EmancipatedBlockEntity.class, OPTIONAL_NBT);
    private BlockState emancipatedState = Blocks.AIR.getDefaultState();

    public EmancipatedBlockEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public static EmancipatedBlockEntity spawn(World world, BlockPos emancipatedPos, BlockState emancipatedState, @Nullable BlockEntity emancipatedBlockEntity, int decayTime) {
        var emancipated = AffinityEntities.EMANCIPATED_BLOCK.create(world);
        emancipated.setPos(emancipatedPos.getX() + .5, emancipatedPos.getY(), emancipatedPos.getZ() + .5);
        emancipated.setEmancipatedState(emancipatedState);
        emancipated.setMaxAge(decayTime);

        if (emancipatedBlockEntity != null) {
            emancipated.setEmancipatedBlockEntityData(emancipatedBlockEntity.createNbtWithId());
        }

        world.spawnEntity(emancipated);
        return emancipated;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(MAX_AGE, 15);
        this.dataTracker.startTracking(EMANCIPATED_BLOCK_ENTITY_DATA, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient && this.age >= this.maxAge()) {
            this.discard();
        }
    }

    public BlockState emancipatedState() {
        return this.emancipatedState;
    }

    public void setEmancipatedState(BlockState emancipatedState) {
        this.emancipatedState = emancipatedState;
    }

    public @Nullable NbtCompound emancipatedBlockEntityData() {
        return this.dataTracker.get(EMANCIPATED_BLOCK_ENTITY_DATA).orElse(null);
    }

    private void setEmancipatedBlockEntityData(@Nullable NbtCompound emancipatedBlockEntity) {
        this.dataTracker.set(EMANCIPATED_BLOCK_ENTITY_DATA, Optional.ofNullable(emancipatedBlockEntity));
    }

    public int maxAge() {
        return this.dataTracker.get(MAX_AGE);
    }

    public void setMaxAge(int maxAge) {
        this.dataTracker.set(MAX_AGE, maxAge);
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("entity.affinity.emancipated_block_state", this.emancipatedState.getBlock().getName());
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put(EMANCIPATED_STATE_KEY, this.emancipatedState);
        nbt.putIfNotNull(EMANCIPATED_BLOCK_ENTITY_DATA_KEY, this.emancipatedBlockEntityData());
        nbt.put(MAX_AGE_KEY, this.maxAge());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.emancipatedState = nbt.getOr(EMANCIPATED_STATE_KEY, Blocks.AIR.getDefaultState());
        this.setEmancipatedBlockEntityData(nbt.getOr(EMANCIPATED_BLOCK_ENTITY_DATA_KEY, null));
        this.setMaxAge(nbt.getOr(MAX_AGE_KEY, 15));
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, Block.getRawIdFromState(this.emancipatedState));
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.emancipatedState = Block.getStateFromRawId(packet.getEntityData());
    }
}
