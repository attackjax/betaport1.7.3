package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.content.BetaLater;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureFlags.class)
abstract class FeatureFlagsMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/flag/FeatureFlagRegistry$Builder;build()Lnet/minecraft/world/flag/FeatureFlagRegistry;"))
    private static FeatureFlagRegistry b173$addLater(FeatureFlagRegistry.Builder builder,
                                                     Operation<FeatureFlagRegistry> original) {
        BetaLater.flag = builder.create(BetaLater.ID);
        return original.call(builder);
    }
}
