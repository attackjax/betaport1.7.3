package dev.b173.mixin.crafting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.Beta173Mod;
import dev.b173.rules.BetaRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(ServerRecipeBook.class)
abstract class RecipeBookForgetMixin {
    @Unique
    private int b173$forgotten;

    @WrapOperation(method = "loadRecipes", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void b173$quietlyForget(Logger logger, String message, Object recipe, Operation<Void> original) {
        if (BetaRules.betaGame() && recipe instanceof ResourceKey<?> key
                && key.identifier().getNamespace().equals("minecraft")) {
            b173$forgotten++;
        } else {
            original.call(logger, message, recipe);
        }
    }

    @Inject(method = "loadRecipes", at = @At("RETURN"))
    private void b173$sayForgotten(List<ResourceKey<Recipe<?>>> recipes, Consumer<ResourceKey<Recipe<?>>> add,
            Predicate<ResourceKey<Recipe<?>>> known, CallbackInfo ci) {
        if (b173$forgotten > 0) {
            Beta173Mod.LOGGER.info("Recipe book: dropped {} of this game's recipes; this world crafts with Beta 1.7.3's",
                    b173$forgotten);
            b173$forgotten = 0;
        }
    }
}
