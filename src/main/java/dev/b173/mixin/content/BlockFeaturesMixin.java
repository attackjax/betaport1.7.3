package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.content.BetaLater;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
abstract class BlockFeaturesMixin {
    @Unique
    private volatile byte b173$kind;

    @ModifyReturnValue(method = "requiredFeatures", at = @At("RETURN"))
    private FeatureFlagSet b173$later(FeatureFlagSet features) {
        byte kind = b173$kind;
        if (kind == 0 && (Object) this instanceof Block block) {
            Boolean beta = BetaLater.beta(block);
            if (beta == null) {
                return features;
            }
            kind = beta ? (byte) 1 : (byte) 2;
            b173$kind = kind;
        }
        return kind == 2 ? BetaLater.set() : features;
    }
}
