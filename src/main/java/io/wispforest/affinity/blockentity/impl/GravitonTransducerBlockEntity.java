package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.item.AttunedShardItem;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class GravitonTransducerBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity, InteractableBlockEntity, InquirableOutlineProvider {

    private static final KeyedEndec<ItemStack> SHARD_KEY = MinecraftEndecs.ITEM_STACK.keyed("Shard", ItemStack.EMPTY);

    @NotNull private ItemStack shard = ItemStack.EMPTY;
    private final SingleStackStorageProvider shardStorage = new SingleStackStorageProvider(() -> this.shard, stack -> this.shard = stack, this::markDirty)
            .canInsert(itemVariant -> !AttunedShardTier.forItem(itemVariant.getItem()).isNone())
            .capacity(1);

    private int time = 0;

    public GravitonTransducerBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.GRAVITON_TRANSDUCER, pos, state);

        this.fluxStorage.setFluxCapacity(64000);
        this.fluxStorage.setMaxExtract(1000);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void tickClient() {
        if (this.shard.isEmpty() || this.world.random.nextFloat() < .75f) return;

        ClientParticles.setParticleCount(3);
        ClientParticles.spawn(new DustColorTransitionParticleEffect(MathUtil.rgbToVec3f(0x7091F5), new Vector3f(1f), 1f), this.world, Vec3d.ofCenter(this.pos), .4f);
    }

    @Override
    public void tickServer() {
        if (this.shard.isEmpty() || this.time++ % 20 != 0) return;

        var generationFactor = AttunedShardTier.forItem(this.shard.getItem()).maxTransfer();
        if (this.fluxCapacity() - this.flux() < generationFactor) return;

        var bullets = this.world.getEntitiesByClass(ShulkerBulletEntity.class, new Box(this.pos).expand(3), $ -> true);
        if (bullets.isEmpty()) return;

        AffinityParticleSystems.TRANSDUCE_SHULKER_BULLET.spawn(this.world, MathUtil.entityCenterPos(bullets.get(0)), this.pos);
        bullets.get(0).kill();

        this.updateFlux(this.flux() + generationFactor);

        AttunedShardItem.damageShard(this.shard, (.1f + this.world.random.nextFloat() * .1f) / 100f);
        if (AttunedShardItem.getShardHealth(this.shard) == 0) {
            this.shard = ItemStack.EMPTY;
            this.markDirty();

            WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_GRAVITON_TRANSDUCER_SHARD_BREAKS, SoundCategory.BLOCKS);
        }
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractionUtil.handleSingleItemContainer(
                world, pos, player, hand,
                stack -> !AttunedShardTier.forItem(stack.getItem()).isNone(),
                InteractionUtil.InvalidBehaviour.DO_NOTHING,
                () -> this.shard,
                stack -> this.shard = stack,
                this::markDirty
        );
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.shard = nbt.get(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), SHARD_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), SHARD_KEY, this.shard);
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.shard);
    }

    public @NotNull ItemStack shard() {
        return this.shard;
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.symmetrical(3, 3, 3);
    }

    static {
        ItemStorage.SIDED.registerForBlockEntity((entity, direction) -> entity.shardStorage, AffinityBlocks.Entities.GRAVITON_TRANSDUCER);
    }
}
