package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class KinesisStaffItem extends StaffItem {

    private static final NbtKey<Integer> ACTIVE_TARGET_ENTITY = new NbtKey<>("TargetEntity", NbtKey.Type.INT);
    private static final TagKey<EntityType<?>> IMMUNE_ENTITIES = TagKey.of(RegistryKeys.ENTITY_TYPE, Affinity.id("kinesis_staff_immune"));

    public KinesisStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks) {
        var playerFacing = player.getRotationVec(0);
        Entity entity;

        if (stack.has(ACTIVE_TARGET_ENTITY)) {
            entity = world.getEntityById(stack.get(ACTIVE_TARGET_ENTITY));
        } else {
            final double reach = 25;
            var maxReach = playerFacing.multiply(reach);

            MixinHooks.INCREASED_TARGETING_MARGIN = true;
            var entityTarget = ProjectileUtil.raycast(
                    player,
                    player.getEyePos(),
                    player.getEyePos().add(maxReach),
                    player.getBoundingBox().stretch(maxReach),
                    candidate -> {
                        if (candidate.isSpectator()) return false;
                        return !candidate.getType().isIn(IMMUNE_ENTITIES);
                    },
                    reach * reach
            );
            MixinHooks.INCREASED_TARGETING_MARGIN = false;

            if (entityTarget == null) return TypedActionResult.pass(stack);
            entity = entityTarget.getEntity();

            if (!world.isClient) stack.put(ACTIVE_TARGET_ENTITY, entity.getId());
        }

        if (entity == null) {
            stack.delete(ACTIVE_TARGET_ENTITY);
            return TypedActionResult.pass(stack);
        }

        var targetPos = player.getEyePos().add(playerFacing.multiply(2.5)).subtract(0, entity.getHeight() / 2, 0);
        var targetVelocity = targetPos.subtract(entity.getPos()).multiply(.25f);
        entity.setVelocity(targetVelocity);
        entity.fallDistance = 0;

        return TypedActionResult.success(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        stack.delete(ACTIVE_TARGET_ENTITY);
    }

    @Override
    protected boolean isContinuous(ItemStack stack) {
        return true;
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 0.05f;
    }

    static {
        AffinityNetwork.CHANNEL.registerServerbound(YeetPacket.class, (message, access) -> {
            var player = access.player();
            var activeStack = player.getActiveItem();
            if (!activeStack.isOf(AffinityItems.KINESIS_STAFF)) return;

            var targetEntity = player.world.getEntityById(activeStack.get(ACTIVE_TARGET_ENTITY));
            if (targetEntity == null) return;

            activeStack.delete(ACTIVE_TARGET_ENTITY);
            player.stopUsingItem();
            player.getItemCooldownManager().set(AffinityItems.KINESIS_STAFF, 10);

            targetEntity.addVelocity(player.getRotationVec(0).multiply(2.5f));
        });
    }

    public record YeetPacket() {}
}
