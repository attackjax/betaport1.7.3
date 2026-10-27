package dev.b173.rules.ai;

import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class BetaWolves {
    private BetaWolves() {
    }

    public static InteractionResult interact(Wolf wolf, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        Level level = wolf.level();
        BetaWolfBrain brain = BetaBrains.brainOf(wolf) instanceof BetaWolfBrain wolfBrain ? wolfBrain : null;
        if (!wolf.isTame()) {
            if (held.is(Items.BONE) && !wolf.isAngry()) {
                held.consume(1, player);
                if (level instanceof ServerLevel) {
                    if (wolf.getRandom().nextInt(3) == 0) {
                        wolf.tame(player);
                        if (brain != null) {
                            brain.setPath(null);
                            brain.sit(true);
                        }
                        wolf.setHealth(20.0F);
                        level.broadcastEntityEvent(wolf, (byte) 7);
                    } else {
                        level.broadcastEntityEvent(wolf, (byte) 6);
                    }
                }
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        if ((held.is(Items.PORKCHOP) || held.is(Items.COOKED_PORKCHOP)) && wolf.getHealth() < 20.0F) {
            held.consume(1, player);
            wolf.heal(3.0F);
            return InteractionResult.CONSUME;
        }
        if (wolf.isOwnedBy(player)) {
            if (level instanceof ServerLevel && brain != null) {
                brain.sit(!wolf.isInSittingPose());
                wolf.setJumping(false);
                brain.setPath(null);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static void alert(Player player, LivingEntity attacker, boolean playerAttacking) {
        if (!(player.level() instanceof ServerLevel level) || !BetaRules.active(level.getServer())) {
            return;
        }
        if (attacker instanceof Creeper || attacker instanceof Ghast) {
            return;
        }
        if (attacker instanceof Wolf wolf && wolf.isTame() && wolf.isOwnedBy(player)) {
            return;
        }
        if (attacker instanceof Player && !level.isPvpAllowed()) {
            return;
        }
        for (Wolf wolf : level.getEntitiesOfClass(Wolf.class,
                BetaWolfBrain.betaBox(player.getX(), player.getY() + BetaBrain.PLAYER_POS_Y, player.getZ()))) {
            if (wolf.isTame() && BetaBrains.brainOf(wolf) instanceof BetaWolfBrain brain && brain.target == null
                    && wolf.isOwnedBy(player) && (!playerAttacking || !wolf.isInSittingPose())) {
                brain.sit(false);
                brain.target = attacker;
            }
        }
    }
}
