package io.wispforest.affinity.blockentity.impl;

import com.mojang.authlib.GameProfile;
import io.wispforest.affinity.block.impl.VillagerArmatureBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.mixin.access.LivingEntityAccessor;
import io.wispforest.affinity.mixin.access.ServerPlayerInteractionManagerAccessor;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EquipmentSlot;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class VillagerArmatureBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, InWorldTooltipProvider {

    public final AnimationState punchAnimationState = new AnimationState();

    private static final KeyedEndec<ItemStack> HELD_STACK = MinecraftEndecs.ITEM_STACK.keyed("held_stack", ItemStack.EMPTY);

    private final GameProfile playerProfile = new GameProfile(UUID.randomUUID(), "villager_armature");

    private ItemStack heldStack = ItemStack.EMPTY;
    private final SingleStackStorageProvider storage = new SingleStackStorageProvider(this::heldStack, this::setHeldStack, this::markDirty);

    private Action action = Action.USE;

    private int time = 0;
    private BlockPos miningPos = BlockPos.ORIGIN;

    public VillagerArmatureBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.VILLAGER_ARMATURE, pos, state);
    }

    @Override
    public void tickServer() {
        this.time++;

        var player = FakePlayer.get((ServerWorld) this.world, this.playerProfile);
        player.interactionManager.update();
        ((LivingEntityAccessor) player).affinity$setLastAttackedTicks(((LivingEntityAccessor) player).affinity$getLastAttackedTicks() + 1);

        switch (this.action) {
            case USE -> {
                if (this.time % 5 == 0 && this.useItem(false).isAccepted()) {
                    this.punch();
                }
            }
            case ATTACK -> {
                if (this.attack().isAccepted()) {
                    this.punch();
                }
            }
            case BREAK -> {
                if (this.breakBlock().isAccepted() && this.time % 4 == 0) {
                    this.punch();
                }
            }
        }
    }

    @Override
    public void tickClient() {
        this.time++;
    }

    public ActionResult useItem(boolean sneak) {
        var player = this.preparePlayer();
        if (player == null) return ActionResult.PASS;

        var blockHit = this.raycastBlock(player);
        if (blockHit == null) return ActionResult.PASS;

        player.setSneaking(sneak);

        var itemResult = player.interactionManager.interactItem(player, player.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND);
        if (itemResult.isAccepted()) {
            this.updateItem();
            return itemResult;
        }

        var blockResult = player.interactionManager.interactBlock(player, player.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND, blockHit);
        if (blockResult.isAccepted()) {
            this.updateItem();
            return blockResult;
        }

        return ActionResult.PASS;
    }

    public ActionResult breakBlock() {
        var player = this.preparePlayer();
        if (player == null) return ActionResult.PASS;

        var blockHit = this.raycastBlock(player);
        if (blockHit == null) return ActionResult.PASS;

        var breakProgress = ((ServerPlayerInteractionManagerAccessor) player.interactionManager).affinity$blockBreakingProgress();

        if (breakProgress < 0 || breakProgress >= 10 || !this.miningPos.equals(blockHit.getBlockPos())) {
            if (breakProgress >= 10) {
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

        this.updateItem();
        return ActionResult.SUCCESS;
    }

    public ActionResult attack() {
        var player = this.preparePlayer();
        if (player == null) return ActionResult.PASS;

        if (player.getAttackCooldownProgress(.5f) < .9f) return ActionResult.PASS;

        var entityResult = InteractionUtil.raycastEntities(player, 1f, player.getEntityInteractionRange(), .5f, entity -> entity.isAlive() && entity.isAttackable());
        if (entityResult == null) return ActionResult.PASS;

        player.attack(entityResult.getEntity());
        AffinityNetwork.CHANNEL.serverHandle(this).send(new PunchPacket(this.pos));

        return ActionResult.SUCCESS;
    }

    private FakePlayer preparePlayer() {
        if (!(this.world instanceof ServerWorld serverWorld)) return null;

        var facing = this.getCachedState().get(VillagerArmatureBlock.FACING);
        var playerPos = Vec3d.ofCenter(this.pos.down(), 0).add(facing.getOffsetX() * .5, 0, facing.getOffsetZ() * .5);

        var fakePlayer = FakePlayer.get(serverWorld, this.playerProfile);
        fakePlayer.refreshPositionAndAngles(playerPos.x, playerPos.y, playerPos.z, facing.asRotation(), 0);
        fakePlayer.setHeadYaw(fakePlayer.getYaw());
        fakePlayer.setOnGround(true);

        fakePlayer.getInventory().selectedSlot = 0;
        fakePlayer.equipStack(EquipmentSlot.MAINHAND, this.heldStack);
        ((LivingEntityAccessor) fakePlayer).affinity$sendEquipmentChanges();

        return fakePlayer;
    }

    private void updateItem() {
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
        return InteractionUtil.handleSingleItemContainer(this.world, this.pos, player, hand, this::heldStack, this::setHeldStack, this::markDirty);
    }

    public ActionResult onScroll(boolean direction) {
        var actionIdx = this.action.ordinal() + (direction ? 1 : -1);
        if (actionIdx > Action.values().length) actionIdx -= Action.values().length;
        if (actionIdx < 0) actionIdx += Action.values().length;

        this.action = Action.values()[actionIdx];
        this.markDirty();

        return ActionResult.SUCCESS;
    }

    public int age() {
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
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), HELD_STACK, this.heldStack);
    }

    public static void initNetwork() {
        AffinityNetwork.CHANNEL.registerClientbound(PunchPacket.class, (message, access) -> {
            if (!(access.player().getWorld().getBlockEntity(message.armaturePos) instanceof VillagerArmatureBlockEntity armature)) return;
            armature.punchAnimationState.start(armature.time);
        });
    }

    private enum Action {
        USE, BREAK, ATTACK;
        public static final Endec<Action> ENDEC = Endec.forEnum(Action.class);
    }

    public record PunchPacket(BlockPos armaturePos) {}

    static {
        ItemStorage.SIDED.registerForBlockEntity((armature, direction) -> armature.storage, AffinityBlocks.Entities.VILLAGER_ARMATURE);
    }
}
