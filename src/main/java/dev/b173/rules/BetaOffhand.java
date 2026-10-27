package dev.b173.rules;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class BetaOffhand {
    private BetaOffhand() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(BetaOffhand::tick);
    }

    private static void tick(ServerLevel level) {
        if (!BetaRules.betaWorld(level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            ItemStack held = player.getOffhandItem();
            if (held.isEmpty()) {
                continue;
            }
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            if (!player.getInventory().add(held) || !held.isEmpty()) {
                player.drop(held, false);
            }
        }
    }
}
