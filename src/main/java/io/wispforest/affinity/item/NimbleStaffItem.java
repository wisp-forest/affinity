package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NimbleStaffItem extends Item {

    public NimbleStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var playerStack = user.getStackInHand(hand);

        var target = user.raycast(50, 0, false);
        if (!(target instanceof BlockHitResult blockHit)) return TypedActionResult.fail(playerStack);
        if (world.isAir(blockHit.getBlockPos())) return TypedActionResult.fail(playerStack);

        var targetCenter = Vec3d.ofCenter(blockHit.getBlockPos());

        final var velocity = targetCenter.subtract(user.getPos()).multiply(
                user.isOnGround()
                        ? 0.15
                        : user.isFallFlying() ? 0.025 : 0.1
        );
        user.addVelocity(velocity.x, velocity.y, velocity.z);

        if (world.isClient) return TypedActionResult.success(playerStack);
        AffinityParticleSystems.WISP_ATTACK.spawn(world, user.getEyePos(), new AffinityParticleSystems.LineData(
                targetCenter, 0xFEF5AC
        ));

        return TypedActionResult.success(playerStack);
    }
}
