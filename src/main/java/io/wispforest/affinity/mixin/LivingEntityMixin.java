package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.enchantment.impl.BastionEnchantment;
import io.wispforest.affinity.enchantment.impl.CriticalGambleEnchantment;
import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.LivingEntityTickCallback;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

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
    public abstract double getAttributeValue(EntityAttribute attribute);

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntityTickCallback.EVENT.invoker().onTick((LivingEntity) (Object) this);
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void applyLifeLeech(DamageSource source, float amount, CallbackInfo ci) {
        if (!(source.getAttacker() instanceof PlayerEntity player)) return;

        if (player.hasStatusEffect(AffinityStatusEffects.LIFE_LEECH)) {
            player.heal(amount * 0.1f * (player.getStatusEffect(AffinityStatusEffects.LIFE_LEECH).getAmplifier() + 1));
        }

        if (!AffinityEntityAddon.getData(player, ArtifactBladeItem.DID_CRIT)) return;

        var weapon = player.getMainHandStack();
        if (weapon.getItem() instanceof ArtifactBladeItem blade && ArtifactBladeItem.getAbilityTicks(player.getWorld(), weapon) >= 0 && blade.tier.ordinal() >= 2) {
            player.heal(amount * .1f);
        }
    }

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void doNotWearLeatherHats(CallbackInfoReturnable<Boolean> cir) {
        if (!this.hasStatusEffect(AffinityStatusEffects.FREEZING)) return;
        cir.setReturnValue(true);
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onItemEquip(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        for (var enchantment : EnchantmentHelper.get(itemStack2).keySet()) {
            if (!(enchantment instanceof EnchantmentEquipEventReceiver receiver)) continue;
            receiver.onEquip((LivingEntity) (Object) this, equipmentSlot, itemStack2);
        }
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onItemUnequip(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        for (var enchantment : EnchantmentHelper.get(itemStack).keySet()) {
            if (!(enchantment instanceof EnchantmentEquipEventReceiver receiver)) continue;
            receiver.onUnequip((LivingEntity) (Object) this, equipmentSlot, itemStack);
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float bastionDamagePenalty(float damage, DamageSource source) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return damage;

        if (AffinityEntityAddon.hasData(attacker, BastionEnchantment.BASTION)) {
            return damage * 0.5f;
        } else {
            return damage;
        }
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void criticalGambleDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

        if (AffinityEntityAddon.hasData(attacker, CriticalGambleEnchantment.ACTIVATED_AT)) {
            long critTick = AffinityEntityAddon.removeData(attacker, CriticalGambleEnchantment.ACTIVATED_AT);
            if (critTick != this.getWorld().getTime() || this.getType().isIn(CriticalGambleEnchantment.BLACKLIST)) return;

            affinity$killWithAttacker((LivingEntity) (Object) this, attacker);
        }
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void executeDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

        if (EnchantmentHelper.getLevel(AffinityEnchantments.EXECUTE, attacker.getMainHandStack()) > 0) {
            if (this.getHealth() >= this.getMaxHealth() * .1) return;

            affinity$killWithAttacker((LivingEntity) (Object) this, attacker);
        }
    }

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void disableLootIfNecessary(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (AffinityComponents.ENTITY_FLAGS.get(this).hasFlag(EntityFlagComponent.NO_DROPS)) {
            ci.cancel();
        }
    }

    @Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
    private void disableXpIfNecessary(CallbackInfo ci) {
        if (AffinityComponents.ENTITY_FLAGS.get(this).hasFlag(EntityFlagComponent.NO_DROPS)) {
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
        this.addStatusEffect(new StatusEffectInstance(AffinityStatusEffects.IMPENDING_DOOM, 300));

        AffinityParticleSystems.AETHUM_OVERCHARGE.spawn(this.getWorld(), this.getPos(), this.getId());

        if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(AffinityItems.AETHUM_OVERCHARGER));
            AffinityCriteria.USED_OVERCHARGER.trigger(serverPlayer);
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void injectAffinityAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(AffinityEntityAttributes.DAMAGE_TAKEN, 0)
                .add(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY, 0)
                .add(AffinityEntityAttributes.FALL_RESISTANCE, 0);
    }

    @ModifyVariable(method = "applyArmorToDamage", at = @At(value = "LOAD", ordinal = 1), argsOnly = true)
    private float applyEmeraldArmorToDamage(float amount) {
        return amount + (float) this.getAttributeValue(AffinityEntityAttributes.DAMAGE_TAKEN);
    }

    @ModifyVariable(method = "takeKnockback", at = @At(value = "STORE", ordinal = 0), ordinal = 0, argsOnly = true)
    private double applyEmeraldArmorToKnockback(double strength) {
        return strength + this.getAttributeValue(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY);
    }

    @ModifyVariable(method = "handleFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyFallResistance(float fallDistance) {
        return fallDistance - (float) this.getAttributeValue(AffinityEntityAttributes.FALL_RESISTANCE);
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
