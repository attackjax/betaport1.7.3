package dev.b173.mixin;

import dev.b173.world.BetaChunkGenerator;
import dev.b173.world.BetaNetherGenerator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(WorldDimensions.class)
abstract class WorldDimensionsMixin {
    @ModifyExpressionValue(method = "bake", at = @At(value = "INVOKE",
            target = "Ljava/util/Set;containsAll(Ljava/util/Collection;)Z"))
    private boolean b173$betaIsComplete(boolean all) {
        Map<ResourceKey<LevelStem>, LevelStem> stems = ((WorldDimensions) (Object) this).dimensions();
        return all || stems.containsKey(LevelStem.OVERWORLD) && stems.containsKey(LevelStem.NETHER);
    }

    @Inject(method = "bake", at = @At("HEAD"), cancellable = true)
    private void b173$noEnd(Registry<LevelStem> datapackStems, CallbackInfoReturnable<WorldDimensions.Complete> cir) {
        Map<ResourceKey<LevelStem>, LevelStem> stems = ((WorldDimensions) (Object) this).dimensions();
        if (stems.containsKey(LevelStem.END)) {
            Map<ResourceKey<LevelStem>, LevelStem> beta = new LinkedHashMap<>(stems);
            beta.remove(LevelStem.END);
            cir.setReturnValue(new WorldDimensions(beta).bake(datapackStems));
        }
    }

    @Inject(method = "isStableOverworld", at = @At("HEAD"), cancellable = true)
    private static void b173$betaIsStable(LevelStem stem, CallbackInfoReturnable<Boolean> cir) {
        if (stem.generator() instanceof BetaChunkGenerator) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isStableNether", at = @At("HEAD"), cancellable = true)
    private static void b173$betaNetherIsStable(LevelStem stem, CallbackInfoReturnable<Boolean> cir) {
        if (stem.generator() instanceof BetaNetherGenerator) {
            cir.setReturnValue(true);
        }
    }
}
