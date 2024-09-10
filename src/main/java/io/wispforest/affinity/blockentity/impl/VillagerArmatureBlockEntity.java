package io.wispforest.affinity.blockentity.impl;

import com.mojang.authlib.GameProfile;
import io.wispforest.affinity.block.impl.VillagerArmatureBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.client.screen.VillagerArmatureScreen;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.mixin.access.ExperienceOrbEntityAccessor;
import io.wispforest.affinity.mixin.access.LivingEntityAccessor;
import io.wispforest.affinity.mixin.access.ServerPlayerInteractionManagerAccessor;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class VillagerArmatureBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, InWorldTooltipProvider {

    public final AnimationState punchAnimationState = new AnimationState();

    private static final KeyedEndec<ItemStack> HELD_STACK = MinecraftEndecs.ITEM_STACK.keyed("held_stack", ItemStack.EMPTY);
    private static final KeyedEndec<Action> ACTION = Action.ENDEC.keyed("action", Action.USE);
    private static final KeyedEndec<Vec2f> CLICK_POSITION = EndecUtil.VEC2F_ENDEC.keyed("click_position", new Vec2f(.5f, .5f));

    private final GameProfile playerProfile = new GameProfile(UUID.randomUUID(), "villager_armature");

    private ItemStack heldStack = ItemStack.EMPTY;
    private final SingleStackStorageProvider storage = new SingleStackStorageProvider(this::heldStack, this::setHeldStack, this::markDirty);

    private Action action = Action.USE;
    public Vec2f clickPosition = new Vec2f(.5f, .5f);

    private int time = 0;
    private int lastActionTimestamp = 0;
    private BlockPos miningPos = BlockPos.ORIGIN;

    public VillagerArmatureBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.VILLAGER_ARMATURE, pos, state);
    }

    @Override
    public void tickServer() {
        this.time++;

        var player = this.preparePlayer();
        if (player == null) return;

        player.interactionManager.update();
        ((LivingEntityAccessor) player).affinity$setLastAttackedTicks(((LivingEntityAccessor) player).affinity$getLastAttackedTicks() + 1);

        if (this.time % 3 == 0) {
            this.world.getEntitiesByClass(ExperienceOrbEntity.class, new Box(this.pos).expand(.25), entity -> entity.getExperienceAmount() > 0).stream().findFirst().ifPresent(xpOrb -> {
                var newAmount = ((ExperienceOrbEntityAccessor) xpOrb).affinity$repairPlayerGears(player, xpOrb.getExperienceAmount());
                if (newAmount != xpOrb.getExperienceAmount()) {
                    if (newAmount != 0) {
                        ((ExperienceOrbEntityAccessor) xpOrb).affinity$setAmount(newAmount);
                    } else {
                        var count = ((ExperienceOrbEntityAccessor) xpOrb).affinity$getPickingCount() - 1;
                        ((ExperienceOrbEntityAccessor) xpOrb).affinity$setPickingCount(count);

                        if (count <= 0) xpOrb.discard();
                    }
                }
            });
        }

        if (!this.world.isReceivingRedstonePower(this.pos)) {
            if (this.action == Action.BREAK && this.miningPos != null) {
                player.interactionManager.processBlockBreakingAction(
                    this.miningPos,
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                    this.getCachedState().get(VillagerArmatureBlock.FACING).getOpposite(),
                    this.world.getHeight(),
                    0
                );

                this.miningPos = null;
            }

            return;
        }

        switch (this.action) {
            case USE -> {
                if (this.timeSinceLastAction() >= 5 && this.useItem(player, false).isAccepted()) {
                    this.punch();
                    this.lastActionTimestamp = this.time;
                }
            }
            case ATTACK -> {
                if (this.attack(player).isAccepted()) {
                    this.punch();
                }
            }
            case BREAK -> {
                if (this.breakBlock(player).isAccepted() && this.timeSinceLastAction() >= 4) {
                    this.punch();
                    this.lastActionTimestamp = this.time;
                }
            }
        }
    }

    private long timeSinceLastAction() {
        return this.time - this.lastActionTimestamp;
    }

    @Override
    public void tickClient() {
        this.time++;
    }

    public ActionResult useItem(FakePlayer player, boolean sneak) {
        player.setSneaking(sneak);

        var entityHit = InteractionUtil.raycastEntities(player, 1f, player.getEntityInteractionRange(), .25f, Entity::isAlive);
        if (entityHit != null) {
            var eventResult = UseEntityCallback.EVENT.invoker().interact(player, this.world, Hand.MAIN_HAND, entityHit.getEntity(), entityHit);
            if (eventResult.isAccepted()) {
                this.updateItem(player);
                return eventResult;
            }

            var entityResult = player.interact(entityHit.getEntity(), Hand.MAIN_HAND);
            if (entityResult.isAccepted()) {
                this.updateItem(player);
                return entityResult;
            }
        }

        var blockHit = this.raycastBlock(player);
        if (blockHit == null) return ActionResult.PASS;

        var itemResult = player.interactionManager.interactItem(player, player.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND);
        if (itemResult.isAccepted()) {
            this.updateItem(player);
            return itemResult;
        }

        var blockResult = player.interactionManager.interactBlock(player, player.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND, blockHit);
        if (blockResult.isAccepted()) {
            this.updateItem(player);
            return blockResult;
        }

        return ActionResult.PASS;
    }

    public ActionResult breakBlock(FakePlayer player) {
        var blockHit = this.raycastBlock(player);
        if (blockHit == null) return ActionResult.PASS;

        var breakProgress = ((ServerPlayerInteractionManagerAccessor) player.interactionManager).affinity$blockBreakingProgress();

        if (breakProgress < 0 || breakProgress >= 10 || !blockHit.getBlockPos().equals(this.miningPos)) {
            if (breakProgress >= 10 && miningPos != null) {
                player.interactionManager.processBlockBreakingAction(
                    this.miningPos,
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    blockHit.getSide(),
                    this.world.getHeight(),
                    0
                );
            }

            player.interactionManager.processBlockBreakingAction(
                blockHit.getBlockPos(),
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                blockHit.getSide(),
                this.world.getHeight(),
                0
            );

            this.miningPos = blockHit.getBlockPos();
        }

        this.updateItem(player);
        return ActionResult.SUCCESS;
    }

    public ActionResult attack(FakePlayer player) {
        if (player.getAttackCooldownProgress(.5f) < .9f) return ActionResult.PASS;

        var entityResult = InteractionUtil.raycastEntities(
            player, 1f, player.getEntityInteractionRange(), .5f,
            entity -> entity.isAlive() && entity.isAttackable() && entity.timeUntilRegen <= 10
        );
        if (entityResult == null) return ActionResult.PASS;

        player.attack(entityResult.getEntity());
        AffinityNetwork.CHANNEL.serverHandle(this).send(new PunchPacket(this.pos));

        return ActionResult.SUCCESS;
    }

    private FakePlayer preparePlayer() {
        if (!(this.world instanceof ServerWorld serverWorld)) return null;

        var fakePlayer = FakePlayer.get(serverWorld, this.playerProfile);

        var facing = this.getCachedState().get(VillagerArmatureBlock.FACING);
        var playerPos = this.raycastOrigin().add(0, -fakePlayer.getEyeHeight(fakePlayer.getPose()), 0);

        fakePlayer.refreshPositionAndAngles(playerPos.x, playerPos.y, playerPos.z, facing.asRotation(), 0);
        fakePlayer.setHeadYaw(fakePlayer.getYaw());
        fakePlayer.setOnGround(true);

        fakePlayer.getInventory().selectedSlot = 0;
        fakePlayer.equipStack(EquipmentSlot.MAINHAND, this.heldStack);
        ((LivingEntityAccessor) fakePlayer).affinity$sendEquipmentChanges();

        return fakePlayer;
    }

    public Vec3d raycastOrigin() {
        var facing = this.getCachedState().get(VillagerArmatureBlock.FACING);
        return Vec3d.ofCenter(this.getPos(), this.clickPosition.y).add(
            facing.getOffsetX() * .5 + facing.getOffsetZ() * (this.clickPosition.x - .5),
            0,
            facing.getOffsetZ() * .5 + facing.getOffsetX() * (this.clickPosition.x - .5)
        );
    }

    public Direction facing() {
        return this.getCachedState().get(VillagerArmatureBlock.FACING);
    }

    private void updateItem(FakePlayer player) {
        this.heldStack = player.getMainHandStack();
        if (this.heldStack.isEmpty()) {
            this.heldStack = ItemStack.EMPTY;
        }

        this.markDirty(false);
    }

    private void punch() {
        AffinityNetwork.CHANNEL.serverHandle(this).send(new PunchPacket(this.pos));
    }

    private @Nullable BlockHitResult raycastBlock(FakePlayer player) {
        var fakeHit = player.raycast(player.getBlockInteractionRange(), 1f, false);
        if (!(fakeHit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) return null;

        return blockHit;
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);
        entries.add(Entry.text(Text.empty(), Text.literal(this.action.name())));
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            if (!this.world.isClient) return ActionResult.SUCCESS;

            this.openScreen();
            return ActionResult.SUCCESS;
        }

        return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand, this::heldStack, this::setHeldStack, this::markDirty);
    }

    @Environment(EnvType.CLIENT)
    private void openScreen() {
        MinecraftClient.getInstance().setScreen(new VillagerArmatureScreen(this));
    }

    public ActionResult onScroll(boolean direction) {
        var actionIdx = this.action.ordinal() + (direction ? 1 : -1);
        if (actionIdx >= Action.values().length) actionIdx -= Action.values().length;
        if (actionIdx < 0) actionIdx += Action.values().length;

        this.action = Action.values()[actionIdx];
        this.markDirty();

        return ActionResult.SUCCESS;
    }

    public int time() {
        return this.time;
    }

    public void setHeldStack(ItemStack stack) {
        this.heldStack = stack;
    }

    public ItemStack heldStack() {
        return this.heldStack;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.setHeldStack(nbt.get(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), HELD_STACK));
        this.action = nbt.get(ACTION);
        this.clickPosition = nbt.get(CLICK_POSITION);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), HELD_STACK, this.heldStack);
        nbt.put(ACTION, this.action);
        nbt.put(CLICK_POSITION, this.clickPosition);
    }

    public static void initNetwork() {
        AffinityNetwork.CHANNEL.registerClientbound(PunchPacket.class, (message, access) -> {
            if (!(access.player().getWorld().getBlockEntity(message.armaturePos) instanceof VillagerArmatureBlockEntity armature)) return;
            armature.punchAnimationState.start(armature.time);
        });

        AffinityNetwork.CHANNEL.registerServerbound(SetClickPositionPacket.class, SetClickPositionPacket.ENDEC, (message, access) -> {
            if (!(access.player().getWorld().getBlockEntity(message.armaturePos) instanceof VillagerArmatureBlockEntity armature)) return;

            armature.clickPosition = message.clickPosition;
            armature.markDirty();
        });
    }

    private enum Action {
        USE, BREAK, ATTACK;
        public static final Endec<Action> ENDEC = Endec.forEnum(Action.class);
    }

    public record PunchPacket(BlockPos armaturePos) {}

    public record SetClickPositionPacket(BlockPos armaturePos, Vec2f clickPosition) {
        public static final StructEndec<SetClickPositionPacket> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.BLOCK_POS.fieldOf("armature_pos", SetClickPositionPacket::armaturePos),
            EndecUtil.VEC2F_ENDEC.fieldOf("click_position", SetClickPositionPacket::clickPosition),
            SetClickPositionPacket::new
        );
    }
    static {
        ItemStorage.SIDED.registerForBlockEntity((armature, direction) -> armature.storage, AffinityBlocks.Entities.VILLAGER_ARMATURE);
    }
}
