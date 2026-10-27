package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityStatsMixin {
    @ModifyReturnValue(method = "getMaxHealth", at = @At("RETURN"))
    private float b173$betaHealth(float health) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (BetaMobs.isBeta(self.getType()) && BetaRules.betaWorld(self.level())) {
            return BetaMobs.maxHealth(self, health);
        }
        return health;
    }

    @ModifyReturnValue(method = "getArmorValue", at = @At("RETURN"))
    private int b173$noNaturalArmour(int armour) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (armour > 0 && BetaMobs.isBeta(self.getType()) && BetaRules.betaWorld(self.level())) {
            return Math.max(0, armour - Mth.floor(self.getAttributeBaseValue(Attributes.ARMOR)));
        }
        return armour;
    }

    @ModifyReturnValue(method = "canBreatheUnderwater", at = @At("RETURN"))
    private boolean b173$betaBreath(boolean breathes) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (breathes && self.getType() != EntityType.SQUID && BetaMobs.isBeta(self.getType())
                && BetaRules.betaWorld(self.level())) {
            return false;
        }
        return breathes;
    }
}
