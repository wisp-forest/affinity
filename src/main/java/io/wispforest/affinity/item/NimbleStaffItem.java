package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NimbleStaffItem extends StaffItem {

    public NimbleStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks) {
        var target = player.raycast(50, 0, false);
        if (!(target instanceof BlockHitResult blockHit)) return TypedActionResult.fail(stack);
        if (world.isAir(blockHit.getBlockPos())) return TypedActionResult.fail(stack);

        var targetCenter = Vec3d.ofCenter(blockHit.getBlockPos());

        final var velocity = targetCenter.subtract(player.getPos()).multiply(
                player.isOnGround()
                        ? 0.15
                        : player.isFallFlying() ? 0.025 : 0.1
        );
        player.addVelocity(velocity.x, velocity.y, velocity.z);

        if (world.isClient) return TypedActionResult.success(stack);
        AffinityParticleSystems.WISP_ATTACK.spawn(world, player.getEyePos(), new AffinityParticleSystems.LineData(
                targetCenter, 0xFEF5AC
        ));

        return TypedActionResult.success(stack);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return .5f;
    }
}
