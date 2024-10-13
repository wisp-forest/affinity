package io.wispforest.affinity.blockentity.impl;

import com.mojang.authlib.GameProfile;
import io.wispforest.affinity.block.impl.VillagerArmatureBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.client.screen.VillagerArmatureScreen;
import io.wispforest.affinity.item.VillagerArmsItem;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.quack.AffinityServerPlayerInteractionManagerExtension;
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
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public class VillagerArmatureBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, InWorldTooltipProvider {

    public final AnimationState punchAnimationState = new AnimationState();

    private static final KeyedEndec<VillagerData> VILLAGER_DATA = CodecUtils.toEndec(VillagerData.CODEC).keyed("villager_data", new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    private static final KeyedEndec<ItemStack> HELD_STACK = MinecraftEndecs.ITEM_STACK.keyed("held_stack", ItemStack.EMPTY);
    private static final KeyedEndec<Action> ACTION = Action.ENDEC.keyed("action", Action.USE);
    private static final KeyedEndec<RedstoneMode> REDSTONE_MODE = RedstoneMode.ENDEC.keyed("redstone_mode", RedstoneMode.REPEAT);
    private static final KeyedEndec<Boolean> SNEAK = Endec.BOOLEAN.keyed("sneak", false);
    private static final KeyedEndec<Vec2f> CLICK_POSITION = EndecUtil.VEC2F_ENDEC.keyed("click_position", new Vec2f(.5f, .5f));

    private final GameProfile playerProfile = new GameProfile(UUID.randomUUID(), "villager_armature");

    private ItemStack heldStack = ItemStack.EMPTY;
    private final SingleStackStorageProvider storage = new SingleStackStorageProvider(this::heldStack, this::setHeldStack, this::markDirty);

    private VillagerData villagerData = VILLAGER_DATA.defaultValue();

    public Action action = ACTION.defaultValue();
    public RedstoneMode redstoneMode = REDSTONE_MODE.defaultValue();
    public Vec2f clickPosition = CLICK_POSITION.defaultValue();
    public boolean sneak = SNEAK.defaultValue();

    private int time = 0;
    private int lastActionTimestamp = 0;
    private boolean redstoneTriggered = false;
    private BlockPos miningPos = BlockPos.ORIGIN;

    public VillagerArmatureBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.VILLAGER_ARMATURE, pos, state);

        this.fluxStorage.setFluxCapacity(32000);
        this.fluxStorage.setMaxInsert(1000);
    }

    @Override
    public void tickServer() {
        this.time++;

        var player = this.preparePlayer();
        if (player == null) return;

        player.interactionManager.update();
        player.getItemCooldownManager().update();
        ((LivingEntityAccessor) player).affinity$setLastAttackedTicks(((LivingEntityAccessor) player).affinity$getLastAttackedTicks() + 1);

        if (this.time % 20 == 0) {
            for (int i = 0; i < player.getInventory().main.size(); i++) {
                if (i == 0) continue;

                var stack = player.getInventory().main.get(i);
                if (stack.isEmpty()) continue;

                player.getInventory().main.set(i, ItemStack.EMPTY);
                ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), stack);
                this.markDirty();
            }
        }

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

        var shouldBeActive = switch (this.redstoneMode) {
            case ALWAYS_ACTIVE -> true;
            case REPEAT -> this.world.isReceivingRedstonePower(this.pos);
            case IMPULSE -> this.redstoneTriggered;
        } && switch (this.action) {
            case BREAK -> this.flux() >= 10;
            case ATTACK -> this.flux() >= 5;
            case USE -> this.flux() >= 2;
        };

        this.redstoneTriggered = false;
        if (!shouldBeActive) {
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

        player.setSneaking(this.sneak);
        switch (this.action) {
            case USE -> {
                if (this.timeSinceLastAction() >= 5 && this.useItem(player).isAccepted()) {
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

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);

        var villagerData = components.get(VillagerArmsItem.VILLAGER_DATA);
        if (villagerData != null) this.villagerData = villagerData.unwrap();
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(VillagerArmsItem.VILLAGER_DATA, VillagerArmsItem.ArmsData.wrap(this.villagerData));
    }

    private long timeSinceLastAction() {
        return this.time - this.lastActionTimestamp;
    }

    @Override
    public void tickClient() {
        this.time++;
    }

    public ActionResult useItem(FakePlayer player) {
        var entityHit = InteractionUtil.raycastEntities(player, 1f, player.getEntityInteractionRange(), .25f, Entity::isAlive);
        if (entityHit != null) {
            var eventResult = UseEntityCallback.EVENT.invoker().interact(player, this.world, Hand.MAIN_HAND, entityHit.getEntity(), entityHit);
            if (eventResult.isAccepted()) {
                this.updateItem(player);
                this.updateFlux(this.flux() - 2);
                return eventResult;
            }

            var entityResult = player.interact(entityHit.getEntity(), Hand.MAIN_HAND);
            if (entityResult.isAccepted()) {
                this.updateItem(player);
                this.updateFlux(this.flux() - 2);
                return entityResult;
            }
        }

        var itemResult = player.interactionManager.interactItem(player, player.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND);
        if (itemResult.isAccepted()) {
            this.updateItem(player);
            this.updateFlux(this.flux() - 2);
            return itemResult;
        }

        var blockHit = this.raycastBlock(player);
        if (blockHit == null) return ActionResult.PASS;

        var blockResult = player.interactionManager.interactBlock(player, player.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND, blockHit);
        if (blockResult.isAccepted()) {
            this.updateItem(player);
            this.updateFlux(this.flux() - 2);
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
        if (player.getAttackCooldownProgress(.5f) < 1f) return ActionResult.PASS;

        var entityResult = InteractionUtil.raycastEntities(
            player, 1f, player.getEntityInteractionRange(), .5f,
            entity -> entity.isAlive() && entity.isAttackable() && entity.timeUntilRegen <= 10
        );
        if (entityResult == null) return ActionResult.PASS;

        this.updateFlux(this.flux() - 5);

        player.attack(entityResult.getEntity());
        AffinityNetwork.CHANNEL.serverHandle(this).send(new PunchPacket(this.pos));

        return ActionResult.SUCCESS;
    }

    private FakePlayer preparePlayer() {
        if (!(this.world instanceof ServerWorld serverWorld)) return null;

        var fakePlayer = FakePlayer.get(serverWorld, this.playerProfile);
        ((AffinityServerPlayerInteractionManagerExtension) fakePlayer.interactionManager).affinity$setBlockBreakingListener(new BlockBreakingListener(this.pos));

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
        entries.add(Entry.text(Text.empty(), Text.translatable(this.action.translationKey())));
        entries.add(Entry.text(Text.empty(), Text.translatable(this.redstoneMode.translationKey())));
        // TODO translations
//        entries.add(Entry.text(Text.empty(), Text.literal()));
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

    public void redstoneTriggered() {
        this.redstoneTriggered = true;
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

    public VillagerData villagerData() {
        return this.villagerData;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.setHeldStack(nbt.get(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), HELD_STACK));
        this.villagerData = nbt.get(VILLAGER_DATA);
        this.action = nbt.get(ACTION);
        this.redstoneMode = nbt.get(REDSTONE_MODE);
        this.sneak = nbt.get(SNEAK);
        this.clickPosition = nbt.get(CLICK_POSITION);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), HELD_STACK, this.heldStack);
        nbt.put(VILLAGER_DATA, this.villagerData);
        nbt.put(ACTION, this.action);
        nbt.put(REDSTONE_MODE, this.redstoneMode);
        nbt.put(SNEAK, this.sneak);
        nbt.put(CLICK_POSITION, this.clickPosition);
    }

    public static void initNetwork() {
        AffinityNetwork.CHANNEL.registerClientbound(PunchPacket.class, (message, access) -> {
            if (!(access.player().getWorld().getBlockEntity(message.armaturePos) instanceof VillagerArmatureBlockEntity armature)) return;
            armature.punchAnimationState.start(armature.time);
        });

        AffinityNetwork.CHANNEL.registerServerbound(SetPropertiesPacket.class, SetPropertiesPacket.ENDEC, (message, access) -> {
            if (!(access.player().getWorld().getBlockEntity(message.armaturePos) instanceof VillagerArmatureBlockEntity armature)) return;

            armature.action = message.action;
            armature.redstoneMode = message.redstoneMode;
            armature.clickPosition = message.clickPosition;
            armature.sneak = message.sneak;
            armature.markDirty();
        });
    }

    public enum Action {
        USE, BREAK, ATTACK;
        public static final Endec<Action> ENDEC = Endec.forEnum(Action.class);

        public String translationKey() {
            return AffinityBlocks.VILLAGER_ARMATURE.getTranslationKey() + ".action." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum RedstoneMode {
        ALWAYS_ACTIVE, REPEAT, IMPULSE;
        public static final Endec<RedstoneMode> ENDEC = Endec.forEnum(RedstoneMode.class);

        public String translationKey() {
            return AffinityBlocks.VILLAGER_ARMATURE.getTranslationKey() + ".redstone-mode." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    public record PunchPacket(BlockPos armaturePos) {}

    public record SetPropertiesPacket(BlockPos armaturePos, Action action, RedstoneMode redstoneMode, Vec2f clickPosition, boolean sneak) {
        public static final StructEndec<SetPropertiesPacket> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.BLOCK_POS.fieldOf("armature_pos", SetPropertiesPacket::armaturePos),
            Action.ENDEC.fieldOf("action", SetPropertiesPacket::action),
            RedstoneMode.ENDEC.fieldOf("redstone_mode", SetPropertiesPacket::redstoneMode),
            EndecUtil.VEC2F_ENDEC.fieldOf("click_position", SetPropertiesPacket::clickPosition),
            Endec.BOOLEAN.fieldOf("sneak", SetPropertiesPacket::sneak),
            SetPropertiesPacket::new
        );

        public SetPropertiesPacket(VillagerArmatureBlockEntity armature) {
            this(armature.pos, armature.action, armature.redstoneMode, armature.clickPosition, armature.sneak);
        }
    }

    private record BlockBreakingListener(BlockPos armaturePos) implements Consumer<ServerPlayerEntity> {
        @Override
        public void accept(ServerPlayerEntity player) {
            if (!(player.getWorld().getBlockEntity(this.armaturePos) instanceof VillagerArmatureBlockEntity armature)) return;
            armature.updateFlux(armature.flux() - 20);
        }
    }

    static {
        ItemStorage.SIDED.registerForBlockEntity((armature, direction) -> armature.storage, AffinityBlocks.Entities.VILLAGER_ARMATURE);
    }
}
