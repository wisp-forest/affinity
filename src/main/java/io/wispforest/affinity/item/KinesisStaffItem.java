package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.LivingEntityTickEvent;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityCriteria;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class KinesisStaffItem extends StaffItem {

    private static final float ENTITY_THROW_COST = 2.5f;

    private static final NbtKey<Integer> ACTIVE_TARGET_ENTITY = new NbtKey<>("TargetEntity", NbtKey.Type.INT);
    private static final TagKey<EntityType<?>> IMMUNE_ENTITIES = TagKey.of(RegistryKeys.ENTITY_TYPE, Affinity.id("kinesis_staff_immune"));

    private static final EntityAttributeModifier MODIFIER = new EntityAttributeModifier(UUID.fromString("bc21b17e-2832-4762-acef-361df22a96f1"), "", -0.65, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AffinityEntityAddon.DataKey<Long> MODIFIER_APPLIED_TICK = AffinityEntityAddon.DataKey.withNullDefault();

    public static final AffinityEntityAddon.DataKey<UUID> PROJECTILE_THROWER = AffinityEntityAddon.DataKey.withNullDefault();

    public KinesisStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    public boolean canBePlacedOnPedestal() {
        return true;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        slowDownEntities(world, pos);
    }

    @Override
    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        slowDownEntities(world, pos);
    }

    protected static void slowDownEntities(World world, BlockPos pos) {
        for (var entity : world.getNonSpectatingEntities(Entity.class, new Box(pos).expand(4, 4, 4))) {
            if (entity.isSneaking() || !(entity instanceof LivingEntity living)) continue;

            var attributes = living.getAttributes();
            if (!attributes.hasModifierForAttribute(EntityAttributes.GENERIC_MOVEMENT_SPEED, MODIFIER.getId())) {
                attributes.addTemporaryModifiers(ImmutableMultimap.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, MODIFIER));
            }

            AffinityEntityAddon.setData(living, MODIFIER_APPLIED_TICK, world.getTime());

            var drag = entity.getVelocity().multiply(.35f);
            var dragMagnitude = drag.horizontalLength();

            if (dragMagnitude > .1) {
                drag = drag.multiply(.1 / dragMagnitude, 1, .1 / dragMagnitude);
            }

            entity.setVelocity(entity.getVelocity().subtract(drag));
            entity.fallDistance = 0;
        }
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
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

            if (!world.isClient) {
                stack.put(ACTIVE_TARGET_ENTITY, entity.getId());
                AffinityCriteria.KINESIS.trigger((ServerPlayerEntity) player, entity);
            }
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable(
                "item.affinity.kinesis_staff.tooltip.consumption_per_throw",
                ENTITY_THROW_COST
        ));
    }

    public void performThrow(PlayerEntity player, ItemStack stack, PacketByteBuf extraData) {
        var targetEntity = player.world.getEntityById(stack.get(ACTIVE_TARGET_ENTITY));
        if (targetEntity == null) return;

        var aethum = AffinityComponents.PLAYER_AETHUM.get(player);
        if (!aethum.tryConsumeAethum(ENTITY_THROW_COST)) return;

        stack.delete(ACTIVE_TARGET_ENTITY);
        player.stopUsingItem();
        player.getItemCooldownManager().set(stack.getItem(), 10);

        targetEntity.addVelocity(player.getRotationVec(0).multiply(2.5f));
        if (targetEntity instanceof ProjectileEntity) AffinityEntityAddon.setData(targetEntity, PROJECTILE_THROWER, player.getUuid());
    }

    public boolean canThrow(ItemStack stack, PlayerEntity player) {
        return stack.has(ACTIVE_TARGET_ENTITY) && AffinityComponents.PLAYER_AETHUM.get(player).getAethum() >= ENTITY_THROW_COST;
    }

    public void writeExtraThrowData(ItemStack stack, PlayerEntity player, PacketByteBuf buffer) {}

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
            if (!(activeStack.getItem() instanceof KinesisStaffItem staff)) return;

            staff.performThrow(player, activeStack, message.extraData);
        });

        LivingEntityTickEvent.EVENT.register(entity -> {
            if (!AffinityEntityAddon.hasData(entity, MODIFIER_APPLIED_TICK)) return;

            if (entity.world.getTime() - AffinityEntityAddon.getData(entity, MODIFIER_APPLIED_TICK) > 2) {
                AffinityEntityAddon.removeData(entity, MODIFIER_APPLIED_TICK);
                entity.getAttributes().removeModifiers(ImmutableMultimap.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, MODIFIER));
            }
        });
    }

    public record YeetPacket(PacketByteBuf extraData) {}
}
