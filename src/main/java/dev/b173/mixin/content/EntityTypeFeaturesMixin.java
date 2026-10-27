package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.content.BetaLater;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityType.class)
abstract class EntityTypeFeaturesMixin {
    @Unique
    private volatile byte b173$kind;

    @ModifyReturnValue(method = "requiredFeatures", at = @At("RETURN"))
    private FeatureFlagSet b173$later(FeatureFlagSet features) {
        byte kind = b173$kind;
        if (kind == 0) {
            Boolean beta = BetaLater.beta((EntityType<?>) (Object) this);
            if (beta == null) {
                return features;
            }
            kind = beta ? (byte) 1 : (byte) 2;
            b173$kind = kind;
        }
        return kind == 2 ? BetaLater.set() : features;
    }
}
