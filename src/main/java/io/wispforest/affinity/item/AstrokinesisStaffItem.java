package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.nbt.NbtKey;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("UnstableApiUsage")
public class AstrokinesisStaffItem extends KinesisStaffItem {

    public static final NbtKey<Boolean> PERFORMING_ASTROKINESIS = new NbtKey<>("PerformingAstrokinesis", NbtKey.Type.BOOLEAN);
    public static final AffinityEntityAddon.DataKey<Void> CAN_THROW_ASTEROID = AffinityEntityAddon.DataKey.withNullDefault();

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks) {
        if (stack.has(PERFORMING_ASTROKINESIS)) return TypedActionResult.success(stack);

        var superResult = super.executeSpell(world, player, stack, remainingTicks);
        if (superResult.getResult().isAccepted()) return superResult;

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
        world.createExplosion(null, null, null, Vec3d.ofCenter(targetPos), 10, true, World.ExplosionSourceType.TNT);
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
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        stack.delete(PERFORMING_ASTROKINESIS);
    }

    @Override
    public void performThrow(PlayerEntity player, ItemStack stack) {
        if (stack.has(PERFORMING_ASTROKINESIS)) {
            var target = player.raycast(50, 0, false);
            if (!(target instanceof BlockHitResult blockHit)) return;

            var aethum = AffinityComponents.PLAYER_AETHUM.get(player);
            if (!aethum.tryConsumeAethum(10)) return;

            player.world.createExplosion(player, DamageSource.player(player), null, Vec3d.ofCenter(blockHit.getBlockPos()), 5, true, World.ExplosionSourceType.TNT);
            player.stopUsingItem();
            player.getItemCooldownManager().set(AffinityItems.ASTROKINESIS_STAFF, 100);
        } else {
            super.performThrow(player, stack);
        }
    }

    @Override
    public boolean canThrow(ItemStack stack, PlayerEntity player) {
        return stack.has(PERFORMING_ASTROKINESIS)
                ? AffinityEntityAddon.hasData(player, CAN_THROW_ASTEROID) && AffinityComponents.PLAYER_AETHUM.get(player).getAethum() >= 10
                : super.canThrow(stack, player);
    }
}
