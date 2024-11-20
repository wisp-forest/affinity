package io.wispforest.affinity.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.VoidBeaconBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.enchantment.BastionEnchantmentLogic;
import io.wispforest.affinity.enchantment.CriticalGambleEnchantmentLogic;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.misc.callback.ItemEquipEvents;
import io.wispforest.affinity.misc.callback.LivingEntityTickCallback;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.statuseffects.AffinityStatusEffect;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique
    private static final EntityAttributeModifier AETHUM_OVERCHARGED_MODIFIER = new EntityAttributeModifier(
            Affinity.id("aethum_overcharge"), 2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract void kill();

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract boolean clearStatusEffects();

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect);

    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();

    @Shadow
    protected float lastDamageTaken;

    @Shadow public abstract AttributeContainer getAttributes();

    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntityTickCallback.EVENT.invoker().onTick((LivingEntity) (Object) this);
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void applyLifeLeech(DamageSource source, float amount, CallbackInfo ci) {
        if (!(source.getAttacker() instanceof PlayerEntity player)) return;

        RegistryEntry<StatusEffect> lifeLeech = Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.LIFE_LEECH);
        if (player.hasStatusEffect(lifeLeech)) {
            player.heal(amount * 0.1f * (player.getStatusEffect(lifeLeech).getAmplifier() + 1));
        }

        if (!AffinityEntityAddon.getData(player, ArtifactBladeItem.DID_CRIT)) return;

        var weapon = player.getMainHandStack();
        if (weapon.getItem() instanceof ArtifactBladeItem blade && ArtifactBladeItem.getAbilityTicks(player.getWorld(), weapon) >= 0 && blade.tier.ordinal() >= 2) {
            player.heal(amount * .1f);
        }
    }

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void doNotWearLeatherHats(CallbackInfoReturnable<Boolean> cir) {
        if (!this.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.FREEZING))) return;
        cir.setReturnValue(true);
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 1))
    private void onItemEquip(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, @Local EquipmentSlot equipmentSlot, @Local ItemStack equippedStack) {
        ItemEquipEvents.EQUIP.invoker().onItemEquip((LivingEntity) (Object) this, equipmentSlot, equippedStack);

//        for (var enchantment : EnchantmentHelper.get(equippedStack).keySet()) {
//            if (!(enchantment instanceof EnchantmentEquipEventReceiver receiver)) continue;
//            receiver.onEquip((LivingEntity) (Object) this, equipmentSlot, equippedStack);
//        }
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private void onItemUnequip(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, @Local EquipmentSlot equipmentSlot, @Local(ordinal = 0) ItemStack unequippedStack) {
        ItemEquipEvents.UNEQUIP.invoker().onItemUnequip((LivingEntity) (Object) this, equipmentSlot, unequippedStack);

//        for (var enchantment : EnchantmentHelper.get(unequippedStack).keySet()) {
//            if (!(enchantment instanceof EnchantmentEquipEventReceiver receiver)) continue;
//            receiver.onUnequip((LivingEntity) (Object) this, equipmentSlot, unequippedStack);
//        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float bastionDamagePenalty(float damage, DamageSource source) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return damage;

        if (AffinityEntityAddon.hasData(attacker, BastionEnchantmentLogic.BASTION)) {
            return damage * 0.5f;
        } else {
            return damage;
        }
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void criticalGambleDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

        if (AffinityEntityAddon.hasData(attacker, CriticalGambleEnchantmentLogic.ACTIVATED_AT)) {
            long critTick = AffinityEntityAddon.removeData(attacker, CriticalGambleEnchantmentLogic.ACTIVATED_AT);
            if (critTick != this.getWorld().getTime() || this.getType().isIn(CriticalGambleEnchantmentLogic.BLACKLIST)) {
                return;
            }

            affinity$killWithAttacker((LivingEntity) (Object) this, attacker);
        }
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void executeDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

        var executeEffectAndLevel = EnchantmentHelper.getEffectListAndLevel(attacker.getMainHandStack(), AffinityEnchantmentEffectComponents.KILL_TARGET_WHEN_LOW_ON_HEALTH);
        if (executeEffectAndLevel != null) {
            if (this.getHealth() >= this.getMaxHealth() * executeEffectAndLevel.getFirst().getValue(executeEffectAndLevel.getSecond())) return;

            affinity$killWithAttacker((LivingEntity) (Object) this, attacker);
        }
    }

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void disableLootIfNecessary(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (this.getComponent(AffinityComponents.ENTITY_FLAGS).hasFlag(EntityFlagComponent.NO_DROPS)) {
            ci.cancel();
        }
    }

    @Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
    private void disableXpIfNecessary(CallbackInfo ci) {
        if (this.getComponent(AffinityComponents.ENTITY_FLAGS).hasFlag(EntityFlagComponent.NO_DROPS)) {
            ci.cancel();
        }
    }

    @Inject(method = "tryUseTotem", at = @At("TAIL"), cancellable = true)
    private void tryUseOvercharger(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;

        ItemStack overcharger = null;

        for (var hand : Hand.values()) {
            var stack = this.getStackInHand(hand);
            if (!stack.isOf(AffinityItems.AETHUM_OVERCHARGER)) continue;

            overcharger = stack;
            break;
        }

        if (overcharger == null) return;
        overcharger.decrement(1);

        this.setHealth(1f);
        this.clearStatusEffects();
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 4));
        this.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.IMPENDING_DOOM), 300));

        AffinityParticleSystems.AETHUM_OVERCHARGE.spawn(this.getWorld(), this.getPos(), this.getId());

        if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(AffinityItems.AETHUM_OVERCHARGER));
            AffinityCriteria.USED_OVERCHARGER.trigger(serverPlayer);

            this.getAttributes().addTemporaryModifiers(ImmutableMultimap.of(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.MAX_AETHUM), AETHUM_OVERCHARGED_MODIFIER));

            var aethum = this.getComponent(AffinityComponents.PLAYER_AETHUM);
            aethum.setAethum(aethum.maxAethum());
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void tryUseVoidBeacon(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!source.getTypeRegistryEntry().matchesKey(DamageTypes.OUT_OF_WORLD) || !((Object) this instanceof ServerPlayerEntity player)) {
            return;
        }

        var beaconPoi = BlockFinder.findPoi(this.getWorld(), AffinityPoiTypes.VOID_BEACON, this.getBlockPos(), 384)
                .min(Comparator.comparingDouble(poi -> poi.getPos().getSquaredDistance(this.getPos())));
        if (beaconPoi.isEmpty()) return;

        var beaconEntity = this.getWorld().getBlockEntity(beaconPoi.get().getPos());
        if (!(beaconEntity instanceof VoidBeaconBlockEntity beacon) || !beacon.active() || beacon.flux() < 64000) {
            return;
        }

        beacon.updateFlux(beacon.flux() - 64000);

        this.setHealth(4f);
        this.clearStatusEffects();
        this.timeUntilRegen = 40;
        this.lastDamageTaken = Float.POSITIVE_INFINITY;

        var targetPos = beaconPoi.get().getPos();
        for (var testPos : BlockPos.iterate(targetPos.up(2), targetPos.up(15))) {
            if (this.getWorld().getBlockState(testPos).isAir()) {
                targetPos = testPos.toImmutable();
                break;
            }
        }

        player.requestTeleport(targetPos.getX() + .5f, targetPos.getY() + .5f, targetPos.getZ() + .5f);
        ServerTasks.doDelayed((ServerWorld) this.getWorld(), 1, () -> AffinityParticleSystems.VOID_BEACON_TELEPORT.spawn(this.getWorld(), this.getPos(), this.getId()));

        cir.setReturnValue(true);
    }

    @Inject(method = "onStatusEffectRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V", shift = At.Shift.AFTER))
    private void passEntityContextToStatusEffect(StatusEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffectType().value() == StatusEffects.GLOWING && (Object) this instanceof PlayerEntity player) {
            player.getComponent(AffinityComponents.GLOWING_COLOR).reset();
        }

        if (effect.getEffectType().value() instanceof AffinityStatusEffect affinityEffect) {
            affinityEffect.onRemovedFromEntity((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void injectAffinityAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.DAMAGE_TAKEN), 0)
                .add(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY), 0)
                .add(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.FALL_RESISTANCE), 0)
                .add(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.EXTRA_ARROW_DAMAGE), 0);
    }

    @ModifyVariable(method = "applyArmorToDamage", at = @At(value = "LOAD", ordinal = 1), argsOnly = true)
    private float applyEmeraldArmorToDamage(float amount) {
        return amount + (float) this.getAttributeValue(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.DAMAGE_TAKEN));
    }

    @ModifyVariable(method = "takeKnockback", at = @At(value = "STORE", ordinal = 0), ordinal = 0, argsOnly = true)
    private double applyEmeraldArmorToKnockback(double strength) {
        return strength + this.getAttributeValue(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY)) * 2;
    }

    @ModifyVariable(method = "handleFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyFallResistance(float fallDistance) {
        return fallDistance - (float) this.getAttributeValue(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.FALL_RESISTANCE));
    }

    @Unique
    private static void affinity$killWithAttacker(LivingEntity victim, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            victim.damage(player.getWorld().getDamageSources().playerAttack(player), Float.MAX_VALUE);
        } else {
            victim.damage(attacker.getWorld().getDamageSources().mobAttack(attacker), Float.MAX_VALUE);
        }
    }
}
