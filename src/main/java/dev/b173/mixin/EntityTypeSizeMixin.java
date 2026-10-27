package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityType.class)
public abstract class EntityTypeSizeMixin {
    @ModifyReturnValue(method = "getDimensions", at = @At("RETURN"))
    private EntityDimensions b173$betaSize(EntityDimensions modern) {
        if (!BetaRules.betaSizes()) {
            return modern;
        }
        return BetaMobs.betaDimensions((EntityType<?>) (Object) this, modern);
    }

    @ModifyReturnValue(method = "getWidth", at = @At("RETURN"))
    private float b173$betaWidth(float modern) {
        EntityType<?> self = (EntityType<?>) (Object) this;
        return BetaRules.betaSizes() && BetaMobs.hasBetaSize(self) ? self.getDimensions().width() : modern;
    }

    @ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
    private float b173$betaHeight(float modern) {
        EntityType<?> self = (EntityType<?>) (Object) this;
        return BetaRules.betaSizes() && BetaMobs.hasBetaSize(self) ? self.getDimensions().height() : modern;
    }
}
