package dev.b173.mixin.crafting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.rules.BetaCrafting;
import dev.b173.rules.BetaRecipeSwitch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerRecipesMixin {
    @WrapOperation(method = {"<init>", "lambda$reloadResources$4"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeManager;finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V"))
    private void b173$tellBetaWorld(RecipeManager recipes, FeatureFlagSet features, Operation<Void> original) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ((BetaRecipeSwitch) recipes).b173$setBetaWorld(BetaCrafting.betaWorld(server.registryAccess()));
        original.call(recipes, features);
    }
}
