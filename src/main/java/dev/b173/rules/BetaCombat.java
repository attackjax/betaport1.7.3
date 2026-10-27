package dev.b173.rules;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.Map;

public final class BetaCombat {
    public static final float ARROW_DAMAGE = 4.0F;
    public static final float BOW_ARROW_SPEED = 1.5F;
    public static final float BOW_ARROW_INACCURACY = 1.0F;

    private static final Map<Item, Integer> WEAPON_DAMAGE = Map.ofEntries(
            Map.entry(Items.WOODEN_SWORD, 4), Map.entry(Items.STONE_SWORD, 6), Map.entry(Items.IRON_SWORD, 8),
            Map.entry(Items.DIAMOND_SWORD, 10), Map.entry(Items.GOLDEN_SWORD, 4),
            Map.entry(Items.WOODEN_AXE, 3), Map.entry(Items.STONE_AXE, 4), Map.entry(Items.IRON_AXE, 5),
            Map.entry(Items.DIAMOND_AXE, 6), Map.entry(Items.GOLDEN_AXE, 3),
            Map.entry(Items.WOODEN_PICKAXE, 2), Map.entry(Items.STONE_PICKAXE, 3), Map.entry(Items.IRON_PICKAXE, 4),
            Map.entry(Items.DIAMOND_PICKAXE, 5), Map.entry(Items.GOLDEN_PICKAXE, 2),
            Map.entry(Items.WOODEN_SHOVEL, 1), Map.entry(Items.STONE_SHOVEL, 2), Map.entry(Items.IRON_SHOVEL, 3),
            Map.entry(Items.DIAMOND_SHOVEL, 4), Map.entry(Items.GOLDEN_SHOVEL, 1),
            Map.entry(Items.WOODEN_HOE, 1), Map.entry(Items.STONE_HOE, 1), Map.entry(Items.IRON_HOE, 1),
            Map.entry(Items.DIAMOND_HOE, 1), Map.entry(Items.GOLDEN_HOE, 1));

    private BetaCombat() {
    }

    public static int weaponDamage(ItemStack stack) {
        Integer damage = WEAPON_DAMAGE.get(stack.getItem());
        if (damage != null) {
            return damage;
        }
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                return -1;
            }
        }
        return 1;
    }

    public static boolean fallingPoint(Player player) {
        return player.getKnownMovement().y <= 0.0D;
    }

    public static InteractionResult shootBow(Level level, Player player, InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        ItemStack ammo = player.getProjectile(bow);
        boolean infinite = player.hasInfiniteMaterials();
        if (ammo.isEmpty() && !infinite) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.FAIL;
        }
        ItemStack shot;
        if (infinite) {
            shot = (ammo.isEmpty() ? new ItemStack(Items.ARROW) : ammo).copyWithCount(1);
            shot.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        } else {
            shot = ammo.split(1);
            if (ammo.isEmpty()) {
                player.getInventory().removeItem(ammo);
            }
        }
        ArrowItem arrowItem = shot.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
        AbstractArrow arrow = arrowItem.createArrow(level, shot, player, bow);
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        float pitch = player.getXRot() * Mth.DEG_TO_RAD;
        arrow.setPos(player.getX() - Mth.cos(yaw) * 0.16F, player.getEyeY() + 0.02D,
                player.getZ() - Mth.sin(yaw) * 0.16F);
        arrow.shoot(-Mth.sin(yaw) * Mth.cos(pitch), -Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch),
                BOW_ARROW_SPEED, BOW_ARROW_INACCURACY);
        arrow.setOldPosAndRot();
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.awardStat(Stats.ITEM_USED.get(bow.getItem()));
        return InteractionResult.CONSUME;
    }

    public static float scaleForDifficulty(Difficulty difficulty, DamageSource source, float amount) {
        if (!(betaAttacker(source) instanceof Monster)) {
            return amount;
        }
        int damage = (int) amount;
        return switch (difficulty) {
            case PEACEFUL -> 0.0F;
            case EASY -> (float) (damage / 3 + 1);
            case NORMAL -> amount;
            case HARD -> (float) (damage * 3 / 2);
        };
    }

    public static Entity betaAttacker(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return source.getDirectEntity() instanceof LivingEntity living ? living : null;
        }
        return source.getEntity();
    }
}
