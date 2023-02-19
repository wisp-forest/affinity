package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
                ? AffinityEntityAddon.hasData(player, CAN_THROW_ASTEROID)
                : super.canThrow(stack, player);
    }
}
