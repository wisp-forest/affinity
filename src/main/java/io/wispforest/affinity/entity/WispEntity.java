package io.wispforest.affinity.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.entity.goal.FlyRandomlyGoal;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public abstract class WispEntity extends PathAwareEntity {

    private static final MutableText DEFAULT_NAME = Text.translatable(Util.createTranslationKey("entity", Affinity.id("wisp")));
    private static final NbtKey<Boolean> MISTY_KEY = new NbtKey<>("Misty", NbtKey.Type.BOOLEAN);

    private static final TrackedData<Boolean> MISTY = DataTracker.registerData(WispEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

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
    protected void initGoals() {
        this.goalSelector.add(10, new FlyRandomlyGoal(this));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(MISTY, true);
    }

    @Override
    public Text getName() {
        return Text.translatable(type().translationKey()).append(" ").append(super.getName());
    }

    @Override
    protected Text getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            ClientParticles.spawnPrecise(this.misty() ? this.particles : ParticleTypes.ASH, this.getWorld(), this.getPos().add(0, .125, 0), .2, .2, .2);
            this.tickClient();
        }
    }

    @Override
    protected void mobTick() {
        this.tickServer();
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        var playerStack = player.getStackInHand(hand);
        if (!playerStack.isOf(Items.GLASS_BOTTLE) || !this.misty()) return ActionResult.PASS;

        ItemOps.decrementPlayerHandItem(player, hand);

        if (!this.getWorld().isClient) {
            player.getInventory().offerOrDrop(this.type().mistItem().getDefaultStack());
            this.dataTracker.set(MISTY, false);
        }

        return ActionResult.SUCCESS;
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

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put(MISTY_KEY, this.misty());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(MISTY, nbt.getOr(MISTY_KEY, true));
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
        return world.getBlockState(pos).isAir() && world.getLightLevel(pos) > 5;
    }
}
