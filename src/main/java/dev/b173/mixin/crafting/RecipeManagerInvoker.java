package dev.b173.mixin.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeManager.class)
public interface RecipeManagerInvoker {
    @Invoker("fromJson")
    static RecipeHolder<?> b173$fromJson(ResourceKey<Recipe<?>> key, JsonObject json, HolderLookup.Provider registries) {
        throw new AssertionError();
    }
}
