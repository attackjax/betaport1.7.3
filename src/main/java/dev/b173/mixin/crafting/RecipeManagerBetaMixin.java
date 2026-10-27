package dev.b173.mixin.crafting;

import dev.b173.rules.BetaCrafting;
import dev.b173.rules.BetaRecipeSwitch;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
abstract class RecipeManagerBetaMixin implements BetaRecipeSwitch {
    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Shadow
    private RecipeMap recipes;

    @Unique
    private Boolean b173$betaWorld;

    @Override
    public void b173$setBetaWorld(boolean beta) {
        b173$betaWorld = beta;
    }

    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    private void b173$betaRecipes(FeatureFlagSet features, CallbackInfo ci) {
        boolean beta = b173$betaWorld != null ? b173$betaWorld : BetaCrafting.betaWorld(registries);
        if (beta) {
            recipes = BetaCrafting.recipes(registries);
        }
    }
}
