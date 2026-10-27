package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.world.BetaDimensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelStorageSource.class)
abstract class LevelStorageNetherMixin {
    @WrapOperation(method = "getLevelDataAndDimensions", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/levelgen/WorldDimensions;bake(Lnet/minecraft/core/Registry;)Lnet/minecraft/world/level/levelgen/WorldDimensions$Complete;"))
    private static WorldDimensions.Complete b173$betaNether(WorldDimensions dimensions, Registry<LevelStem> datapackStems,
                                                           Operation<WorldDimensions.Complete> original,
                                                           @Local(argsOnly = true) HolderLookup.Provider registries) {
        return original.call(BetaDimensions.withBetaNether(dimensions, registries), datapackStems);
    }
}
