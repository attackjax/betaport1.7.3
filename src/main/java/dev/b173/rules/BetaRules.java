package dev.b173.rules;

import dev.b173.rules.ai.BetaBrains;
import dev.b173.world.BetaChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;

public final class BetaRules {
    public static volatile boolean clientInBetaWorld;

    public static volatile boolean serverInBetaWorld;

    private static final int FROZEN_FOOD_LEVEL = 6;

    private static final Map<String, Float> BETA_HEALING = Map.of(
            "apple", 4.0F,
            "mushroom_stew", 10.0F,
            "bread", 5.0F,
            "porkchop", 3.0F,
            "cooked_porkchop", 8.0F,
            "golden_apple", 42.0F,
            "cod", 2.0F,
            "cooked_cod", 5.0F,
            "cookie", 1.0F);

    private BetaRules() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(BetaRules::holdHunger);
        UseItemCallback.EVENT.register(BetaRules::eat);
        UseBlockCallback.EVENT.register(BetaRules::eatCake);
        BetaBrains.init();
        BetaOffhand.register();
        ServerLevelEvents.LOAD.register((server, level) -> {
            if (level.dimension() == Level.OVERWORLD) {
                serverInBetaWorld = level.getChunkSource().getGenerator() instanceof BetaChunkGenerator;
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverInBetaWorld = false);
    }

    public static boolean betaSizes() {
        return serverInBetaWorld || clientInBetaWorld;
    }

    public static boolean betaGame() {
        return serverInBetaWorld || clientInBetaWorld;
    }

    public static boolean active(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld != null && overworld.getChunkSource().getGenerator() instanceof BetaChunkGenerator;
    }

    public static boolean betaWorld(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return active(serverLevel.getServer());
        }
        return level.isClientSide() && clientInBetaWorld;
    }

    private static void holdHunger(MinecraftServer server) {
        if (!active(server)) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            FoodData food = player.getFoodData();
            if (food.getFoodLevel() != FROZEN_FOOD_LEVEL) {
                food.setFoodLevel(FROZEN_FOOD_LEVEL);
            }
            food.setSaturation(0.0F);
        }
    }

    private static InteractionResult eat(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !betaWorld(level)) {
            return InteractionResult.PASS;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        float healing = Math.max(1.0F, food.nutrition());
        if (id.getNamespace().equals("minecraft")) {
            healing = BETA_HEALING.getOrDefault(id.getPath(), healing);
        }
        player.heal(healing);
        if (!player.isCreative()) {
            if (stack.is(Items.MUSHROOM_STEW)) {
                player.setItemInHand(hand, new ItemStack(Items.BOWL));
            } else {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult eatCake(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.CAKE) || !betaWorld(level)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || player.getHealth() >= player.getMaxHealth()) {
            return InteractionResult.SUCCESS;
        }
        player.heal(3.0F);
        int bites = state.getValue(CakeBlock.BITES) + 1;
        if (bites >= 6) {
            level.removeBlock(pos, false);
        } else {
            level.setBlockAndUpdate(pos, state.setValue(CakeBlock.BITES, bites));
        }
        return InteractionResult.SUCCESS;
    }
}
