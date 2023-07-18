package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.ServerTasks;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SoupOfBeeItem extends Item {

    public SoupOfBeeItem() {
        super(AffinityItems.settings(AffinityItemGroup.NATURE).food(
                new FoodComponent.Builder()
                        .hunger(-4)
                        .saturationModifier(2)
                        .statusEffect(new StatusEffectInstance(StatusEffects.POISON, 200), 1f)
                        .alwaysEdible()
                        .build()
        ).maxCount(1));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        user.playSound(SoundEvents.ENTITY_BEE_STING, 1, 1);

        if (!world.isClient) {
            int beeCount = world.random.nextBetween(2, 6);
            for (int i = 0; i < beeCount; i++) {
                var bee = new BeeEntity(EntityType.BEE, world);
                bee.refreshPositionAndAngles(
                        user.getX() + world.random.nextFloat() - .5,
                        (user.getY() + user.getHeight() / 2d) + world.random.nextFloat() - .5,
                        user.getZ() + world.random.nextFloat() - .5,
                        world.random.nextFloat() * 360f, 0
                );

                world.spawnEntity(bee);

                // we kill the bees with one tick of delay to ensure that
                // their model angles update properly on the client
                ServerTasks.doDelayed((ServerWorld) world, 1, () -> bee.setHealth(0));
            }
        }

        return super.finishUsing(stack, world, user);
    }
}
