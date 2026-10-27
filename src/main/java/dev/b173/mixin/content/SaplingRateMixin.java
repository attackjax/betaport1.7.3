package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SaplingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(SaplingBlock.class)
abstract class SaplingRateMixin {
    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 7))
    private int b173$betaChance(int chance, @Local(argsOnly = true) ServerLevel level) {
        return BetaRules.betaWorld(level) ? 30 : chance;
    }
}
