package io.wispforest.affinity.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.goal.FlyRandomlyGoal;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public abstract class WispEntity extends PathAwareEntity implements Vibrations {

    public static final TagKey<GameEvent> RAVE_NOISES = TagKey.of(RegistryKeys.GAME_EVENT, Affinity.id("rave_noises"));

    private static final KeyedEndec<Boolean> MISTY_KEY = Endec.BOOLEAN.keyed("Misty", true);

    private static final TrackedData<Boolean> MISTY = DataTracker.registerData(WispEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Long> LAST_RAVE_TIMESTAMP = DataTracker.registerData(WispEntity.class, TrackedDataHandlerRegistry.LONG);

    private final Vibrations.ListenerData listenerData = new ListenerData();
    private final Vibrations.Callback callback = new VibrationsCallback();
    private final EntityGameEventHandler<Vibrations.VibrationListener> gameEventHandler = new EntityGameEventHandler<>(new VibrationListener(this));

    private final DustParticleEffect particles;

    public WispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        this.particles = new DustParticleEffect(MathUtil.rgbToVec3f(this.type().color()), 1);
        this.moveControl = new FlightMoveControl(this, 75, true);
        this.experiencePoints = 3;
    }

    protected abstract void tickServer();

    protected abstract void tickClient();

    public abstract WispType type();

    @Override
    public boolean canBeLeashed() {
        return !this.isLeashed();
    }

    @Override
    protected Vec3d getLeashOffset() {
        return new Vec3d(0, .1f, 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(10, new FlyRandomlyGoal(this));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MISTY, true);
        builder.add(LAST_RAVE_TIMESTAMP, 0L);
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable(this.type().translationKey()).append(" ").append(Text.translatable("entity.affinity.wisp"));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            var particles = this.misty()
                    ? this.isRaving() ? new DustParticleEffect(MathUtil.rgbToVec3f(Color.ofHsv(this.getWorld().random.nextFloat(), .65f, 1f).rgb()), 1) : this.particles
                    : ParticleTypes.ASH;

            ClientParticles.spawnPrecise(particles, this.getWorld(), this.getPos().add(0, .125, 0), .2, .2, .2);
            this.tickClient();
        }
    }

    @Override
    protected void mobTick() {
        this.tickServer();
        Vibrations.Ticker.tick(this.getWorld(), this.listenerData, this.callback);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        var playerStack = player.getStackInHand(hand);
        if (playerStack.isOf(Items.GLASS_BOTTLE) && this.misty()) {
            ItemOps.decrementPlayerHandItem(player, hand);
            if (!this.getWorld().isClient) {
                player.getInventory().offerOrDrop(this.type().mistItem().getDefaultStack());
                this.dataTracker.set(MISTY, false);
            }

            return ActionResult.SUCCESS;
        } else if (this.getPreferredEquipmentSlot(playerStack) == EquipmentSlot.HEAD
                || playerStack.isEmpty()
                || FabricLoader.getInstance().isModLoaded("wearthat")) {
            var existingHelmet = this.getEquippedStack(EquipmentSlot.HEAD);

            this.equipStack(EquipmentSlot.HEAD, ItemOps.singleCopy(playerStack));
            ItemOps.decrementPlayerHandItem(player, hand);

            if (!existingHelmet.isEmpty()) {
                if (playerStack.isEmpty()) {
                    player.setStackInHand(hand, existingHelmet);
                } else {
                    this.dropStack(existingHelmet);
                }
            }

            this.setEquipmentDropChance(EquipmentSlot.HEAD, 2f);
            this.setPersistent();
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {}

    @Override
    protected boolean shouldDropLoot() {
        return this.misty();
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world) {
            @Override
            public boolean isValidPosition(BlockPos pos) {
                return true;
            }

            @Override
            public void tick() {
                super.tick();

                if (!WispEntity.this.misty()) {
                    WispEntity.this.addVelocity(0, -.005f, 0);
                }
            }
        };
    }

    public boolean isRaving() {
        return this.getWorld().getTime() - this.dataTracker.get(LAST_RAVE_TIMESTAMP) < 40;
    }

    @Override
    public void updateEventHandler(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback) {
        if (this.getWorld() instanceof ServerWorld world) {
            callback.accept(this.gameEventHandler, world);
        }
    }

    @Override
    public ListenerData getVibrationListenerData() {
        return this.listenerData;
    }

    @Override
    public Callback getVibrationCallback() {
        return this.callback;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put(MISTY_KEY, this.misty());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(MISTY, nbt.get(MISTY_KEY));
    }

    public boolean misty() {
        return this.dataTracker.get(MISTY);
    }

    public static DefaultAttributeContainer.Builder createWispAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, .25)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, .6);
    }

    public static <E extends WispEntity> boolean isValidSpawn(EntityType<E> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos).isAir() && world.getLightLevel(LightType.SKY, pos) > 7;
    }

    private final class VibrationsCallback implements Vibrations.Callback {

        private final PositionSource positionSource = new EntityPositionSource(WispEntity.this, WispEntity.this.getHeight() / 2);

        @Override
        public int getRange() {
            return 12;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public TagKey<GameEvent> getTag() {
            return RAVE_NOISES;
        }

        @Override
        public boolean accepts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter) {
            return true;
        }

        @Override
        public void accept(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
            WispEntity.this.dataTracker.set(LAST_RAVE_TIMESTAMP, world.getTime());
        }
    }
}
