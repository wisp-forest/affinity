package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.nbt.NbtKey;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AstrokinesisStaffItem extends KinesisStaffItem {

    private static final int ASTEROID_THROW_COST = 10;

    public static final NbtKey<Boolean> PERFORMING_ASTROKINESIS = new NbtKey<>("PerformingAstrokinesis", NbtKey.Type.BOOLEAN);
    public static final AffinityEntityAddon.DataKey<Float> ASTEROID_ORIGIN = AffinityEntityAddon.DataKey.withNullDefault();

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks) {
        if (stack.has(PERFORMING_ASTROKINESIS)) return TypedActionResult.success(stack);

        var superResult = super.executeSpell(world, player, stack, remainingTicks);
        if (superResult.getResult().isAccepted()) return superResult;

        var raycast = player.raycast(512d, 0f, true);
        if (raycast.getType() != HitResult.Type.MISS && !world.getBlockState(((BlockHitResult) raycast).getBlockPos()).isOf(AffinityBlocks.THE_SKY)) {
            return TypedActionResult.pass(stack);
        }

        stack.put(PERFORMING_ASTROKINESIS, true);
        return TypedActionResult.success(stack);
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        if (pedestal.flux() < pedestal.fluxCapacity()) return;

        var storageBelow = ItemStorage.SIDED.find(world, pos.down(), Direction.UP);
        if (storageBelow == null) return;

        BlockPos targetPos = null;
        for (var view : storageBelow) {
            if (!view.getResource().isOf(Items.ECHO_SHARD)) continue;
            if (!view.getResource().hasNbt()) continue;

            var nbt = view.getResource().getNbt();
            if (!nbt.has(EchoShardExtension.BOUND)) continue;
            if (!nbt.get(EchoShardExtension.WORLD).equals(world.getRegistryKey().getValue())) continue;

            targetPos = nbt.get(EchoShardExtension.POS);
            break;
        }

        if (targetPos == null) return;

        pedestal.consumeFlux(pedestal.fluxCapacity());
        this.spawnAsteroid(world, targetPos, 10f, null, null);

        AffinityParticleSystems.ABERRANT_CALLING_SUCCESS.spawn(world, Vec3d.ofCenter(pos, .95f));
    }

    @Override
    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {}

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return stack.has(PERFORMING_ASTROKINESIS)
                ? 0
                : super.getAethumConsumption(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable(
                "item.affinity.astrokinesis_staff.tooltip.consumption_per_throw",
                ASTEROID_THROW_COST
        ));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        stack.delete(PERFORMING_ASTROKINESIS);
    }

    @Override
    public void performThrow(PlayerEntity player, ItemStack stack, PacketByteBuf extraData) {
        if (stack.has(PERFORMING_ASTROKINESIS)) {
            var target = player.raycast(100, 0, false);
            if (!(target instanceof BlockHitResult blockHit)) return;

            var aethum = AffinityComponents.PLAYER_AETHUM.get(player);
            if (!aethum.tryConsumeAethum(ASTEROID_THROW_COST)) return;

            this.spawnAsteroid(player.world, blockHit.getBlockPos(), 5f, player, extraData.readFloat());

            player.stopUsingItem();
            player.getItemCooldownManager().set(AffinityItems.ASTROKINESIS_STAFF, 100);
        } else {
            super.performThrow(player, stack, extraData);
        }
    }

    private void spawnAsteroid(World world, BlockPos target, float power, @Nullable PlayerEntity thrower, @Nullable Float origin) {
        var asteroid = AffinityEntities.ASTEROID.create(world);

        var spawnPos = Vec3d.ofCenter(target.up(175));
        if (origin != null) {
            spawnPos = spawnPos.add(Vec3d.fromPolar(0, origin).multiply(75));
        } else {
            spawnPos = spawnPos.add(world.random.nextBetween(-100, 100), 0, world.random.nextBetween(-100, 100));
        }

        asteroid.setPosition(spawnPos);
        asteroid.setExplosionPower(power);

        if (thrower != null) asteroid.setOwner(thrower);

        asteroid.setVelocity(Vec3d.ofCenter(target).subtract(spawnPos).multiply(1 / 30d));
        world.spawnEntity(asteroid);
    }

    @Override
    public boolean canThrow(ItemStack stack, PlayerEntity player) {
        return stack.has(PERFORMING_ASTROKINESIS)
                ? AffinityEntityAddon.hasData(player, ASTEROID_ORIGIN) && AffinityComponents.PLAYER_AETHUM.get(player).getAethum() >= ASTEROID_THROW_COST
                : super.canThrow(stack, player);
    }

    @Override
    public void writeExtraThrowData(ItemStack stack, PlayerEntity player, PacketByteBuf buffer) {
        if (!stack.has(PERFORMING_ASTROKINESIS)) return;
        buffer.writeFloat(AffinityEntityAddon.getData(player, ASTEROID_ORIGIN));
    }
}
